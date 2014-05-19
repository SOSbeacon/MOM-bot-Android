package org.cnc.mombotble.activity;

import org.cnc.mombot.R;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombotble.adapter.BleDevicesRecordAdapter;
import org.cnc.mombotble.ble.MyBleSensorsRecordService;
import org.cnc.mombotble.resource.DeviceResource;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceRecordedActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	private static final String TAG = DeviceRecordedActivity.class.getSimpleName();
	private static final int LOADER_GET_LIST_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 1;
	private BleDevicesRecordAdapter leDeviceListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_devices_recording);

		setContentView(R.layout.device_scan_activity);
		final View emptyView = findViewById(R.id.empty_view);
		getListView().setEmptyView(emptyView);

		// init adapter
		if (leDeviceListAdapter == null) {
			leDeviceListAdapter = new BleDevicesRecordAdapter(getBaseContext());
			setListAdapter(leDeviceListAdapter);
		}

		// init loader
		getLoaderManager().initLoader(LOADER_GET_LIST_DEVICE, null, this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// start server
		Intent intent = new Intent(this, MyBleSensorsRecordService.class);
		startService(intent);
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
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
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
			leDeviceListAdapter.clear();
			if (cursor != null && cursor.moveToFirst()) {
				do {
					DeviceResource device = new DeviceResource(cursor);
					leDeviceListAdapter.add(device);
				} while (cursor.moveToNext());
			}
			leDeviceListAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

}