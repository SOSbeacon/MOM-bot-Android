package org.cnc.mombot.ble.activity;

import org.ble.sensortag.ble.BleActionsReceiver;
import org.ble.sensortag.ble.BleServiceListener;
import org.cnc.mombot.R;
import org.cnc.mombot.ble.adapter.BleDevicesRecordAdapter;
import org.cnc.mombot.ble.adapter.BleDevicesRecordAdapter.BleDeviceRecordAdapterCallback;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.ble.service.MultiBleService.BleSensorRecordServiceBinder;
import org.cnc.mombot.ble.service.MyBleSensorsRecordService;
import org.cnc.mombot.provider.DbContract.TableDevice;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceRecordedActivity extends ListActivity implements LoaderCallbacks<Cursor>,
		BleDeviceRecordAdapterCallback, BleServiceListener {
	private static final String TAG = DeviceRecordedActivity.class.getSimpleName();
	private static final int LOADER_GET_LIST_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 1;
	private BleDevicesRecordAdapter mAdapter;
	private BleSensorRecordServiceBinder mBinder;
	private BleActionsReceiver mReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_devices_recording);

		setContentView(R.layout.device_scan_activity);
		final View emptyView = findViewById(R.id.empty_view);
		getListView().setEmptyView(emptyView);

		// init adapter
		if (mAdapter == null) {
			mAdapter = new BleDevicesRecordAdapter(this, this);
			setListAdapter(mAdapter);
		}

		// init loader
		getLoaderManager().initLoader(LOADER_GET_LIST_DEVICE, null, this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// start server
		initService();
	}

	private void initService() {
		// start MediaService
		Intent service = new Intent(this, MyBleSensorsRecordService.class);
		this.startService(service);
		// bind the service with mConnection
		this.bindService(service, mConnection, Context.BIND_AUTO_CREATE);

		// Register mMessageReceiver to receive messages from media service.
		mReceiver = new BleActionsReceiver(this);
		this.registerReceiver(mReceiver, BleActionsReceiver.createIntentFilter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_recording, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			startActivity(new Intent(this, MyDeviceScanActivity.class));
			invalidateOptionsMenu();
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(LOADER_GET_LIST_DEVICE, null, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_CANCELED) {
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unbind the media binder
		try {
			this.unbindService(mConnection);
		} catch (Exception ex) {
		}
		try {
			// unregister the update name broadcast receiver
			this.unregisterReceiver(mReceiver);
		} catch (Exception ex) {
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
		case LOADER_GET_LIST_DEVICE:
			return new CursorLoader(this, TableDevice.CONTENT_URI, null, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (id) {
		case LOADER_GET_LIST_DEVICE:
			mAdapter.clear();
			if (cursor != null && cursor.moveToFirst()) {
				do {
					DeviceResource device = new DeviceResource(cursor);
					mAdapter.add(device);
				} while (cursor.moveToNext());
			}
			mAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// set service binder to media player fragment
			mBinder = (BleSensorRecordServiceBinder) service;
		}

		public void onServiceDisconnected(ComponentName className) {
			// set service binder null
			mBinder = null;
		}
	};

	@Override
	public void removeDevice(String deviceAddress) {
		if (mBinder != null) {
			mBinder.disconnectDevice(deviceAddress);
		}
	}

	@Override
	public void onConnected(String deviceAddress) {
		int size = mAdapter.getCount();
		for (int i = 0; i < size; i++) {
			if (mAdapter.getItem(i).address.equals(deviceAddress)) {
				mAdapter.getItem(i).status = DeviceResource.STATUS_CONNECTED;
				break;
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		int size = mAdapter.getCount();
		for (int i = 0; i < size; i++) {
			if (mAdapter.getItem(i).address.equals(deviceAddress)) {
				mAdapter.getItem(i).status = DeviceResource.STATUS_DISCONNECTED;
				break;
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
	}

	@Override
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
	}

}