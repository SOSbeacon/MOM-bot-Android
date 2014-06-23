package org.cnc.mombot.ble.activity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import org.cnc.mombot.R;
import org.cnc.mombot.ble.adapter.BleDevicesRecordAdapter;
import org.cnc.mombot.ble.adapter.BleDevicesRecordAdapter.BleDeviceRecordAdapterCallback;
import org.cnc.mombot.ble.algorithm.DoorStatusAlgorithm;
import org.cnc.mombot.ble.resource.DataRecordedResource;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.ble.service.MyBleSensorsRecordService;
import org.cnc.mombot.ble.service.MyBleSensorsRecordService.BleSensorRecordServiceBinder;
import org.cnc.mombot.provider.DbContract.TableDataRecorded;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombot.sensorcloud.Device;
import org.cnc.mombot.sensorcloud.Point;
import org.cnc.mombot.sensorcloud.SampleRate;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.DateTimeFormater;
import org.cnc.mombot.utils.SharePrefs;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceRecordedActivity extends ListActivity implements LoaderCallbacks<Cursor>,
		BleDeviceRecordAdapterCallback {
	private static final int LOADER_GET_LIST_DEVICE = 1;
	private static final int LOADER_GET_LIST_REPORT = 2;
	private static final int REQUEST_ENABLE_BT = 1;
	private BleDevicesRecordAdapter mAdapter;
	private BleSensorRecordServiceBinder mBinder;
	private Intent mService;

	// thanhle@cncsoftgroup.com - pass: xxx123
	public static final Device cloudDevice = new Device("OAPI00ZKMN58KXGM",
			"bef002002880bee63e46e6209b536466f67cbbbf9552dbedaa5494afc204140e");

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
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				DeviceResource device = mAdapter.getItem(position);
				Intent intent = new Intent(DeviceRecordedActivity.this, ReportActivity.class);
				intent.putExtra(ReportActivity.EXTRA_DEVICE_ADDRESS, device.address);
				startActivity(intent);
			}
		});

		// init loader
		getLoaderManager().initLoader(LOADER_GET_LIST_DEVICE, null, this);
		getLoaderManager().initLoader(LOADER_GET_LIST_REPORT, null, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// start server
		initService();
	}

	private void initService() {
		// start MediaService
		mService = new Intent(this, MyBleSensorsRecordService.class);
		startService(mService);
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
		if (mService == null) {
			mService = new Intent(this, MyBleSensorsRecordService.class);
		}
		// bind the service with mConnection
		this.bindService(mService, mConnection, Context.BIND_AUTO_CREATE);
		// auto put data to SensorCloud
		putDataToSensorCloud();
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
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
		case LOADER_GET_LIST_DEVICE:
			return new CursorLoader(this, TableDevice.CONTENT_URI, null, null, null, null);
		case LOADER_GET_LIST_REPORT:
			return new CursorLoader(this, TableDataRecorded.CONTENT_URI, null, null, null, TableDataRecorded.TIME_SAVED
					+ " desc");
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
					// get last door status
					String where = TableDataRecorded.ADDRESS + "='" + device.address + "'";
					Cursor c = getContentResolver().query(TableDataRecorded.CONTENT_URI, null, where, null,
							TableDataRecorded.TIME_SAVED + " desc");
					if (c != null) {
						if (c.moveToFirst()) {
							DataRecordedResource data = new DataRecordedResource(c);
							device.doorStatus = data.data;
						}
						c.close();
					}
					mAdapter.add(device);
				} while (cursor.moveToNext());
			}
			mAdapter.notifyDataSetChanged();
			break;
		case LOADER_GET_LIST_REPORT:
			if (cursor != null && cursor.moveToFirst()) {
				// get first row
				DataRecordedResource data = new DataRecordedResource(cursor);
				for (int i = 0; i < mAdapter.getCount(); i++) {
					DeviceResource device = mAdapter.getItem(i);
					if (device.address.equals(data.address)) {
						device.doorStatus = data.data;
						break;
					}
				}
				mAdapter.notifyDataSetChanged();
			}
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
	public void removeDevice(String deviceAddress, String deviceCode) {
		// remove on database
		String where = TableDevice.ADDRESS + "='" + deviceAddress + "'";
		// remove on device table
		getContentResolver().delete(TableDevice.CONTENT_URI, where, null);
		// remove report table
		getContentResolver().delete(TableDataRecorded.CONTENT_URI, where, null);
		// remove door close value on prefs
		SharePrefs.getInstance().saveDoorCloseValue(deviceAddress, 0);
		if (mBinder != null) {
			mBinder.disconnectDevice(deviceAddress);
		}
		// remove device on SensorCloud
		if (!TextUtils.isEmpty(deviceCode)) {
			new Thread(new RemoveSensorRunnable(deviceCode)).start();
		}
	}

	private void putDataToSensorCloud() {
		Cursor cursor = getContentResolver().query(TableDevice.CONTENT_URI, null, null, null, null);
		long time = 0, preTime = 0;
		long SECOND = (long) Consts.SAMPLE_RATE * 1000000000; // 1m in nanosecond
		float value = 0f, preValue = 0f;
		Calendar calendar = Calendar.getInstance();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					DeviceResource device = new DeviceResource(cursor);
					// query all report of this device
					String where = TableDataRecorded.ADDRESS + "='" + device.address + "'";
					Cursor c = getContentResolver().query(TableDataRecorded.CONTENT_URI, null, where, null,
							TableDataRecorded.TIME_SAVED + " ASC");
					ArrayList<Point> points = new ArrayList<Point>();

					if (c != null) {
						if (c.moveToFirst()) {
							DataRecordedResource data = new DataRecordedResource(c);
							if (data.synced == DataRecordedResource.STATE_NOT_SYNCED) {
								try {
									// add 2 first Point before first report 1 second having value 2, -2
									// purpose: to having center graph, because status door has value is 0, 1.
									// we will add 2 fake value at begin of report having value 2, -2
									calendar.setTime(DateTimeFormater.timeServerFormat.parse(data.timeSaved));
									calendar.add(Calendar.SECOND, -2);
									time = calendar.getTimeInMillis() * 1000000;
									// add fake point before first report data 2 second
									points.add(new Point(time, -2));
									calendar.add(Calendar.SECOND, 1);
									time = calendar.getTimeInMillis() * 1000000;
									// add fake point before first report data 1 second
									points.add(new Point(time, 2));
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
							}
							do {
								DataRecordedResource d = new DataRecordedResource(c);
								try {
									// convert time to nanosecond
									time = DateTimeFormater.timeServerFormat.parse(d.timeSaved).getTime() * 1000000;
									if (d.synced == DataRecordedResource.STATE_NOT_SYNCED && preTime != 0) {
										// add missing data between current time and previous time
										for (long i = preTime; i < time; i += SECOND) {
											points.add(new Point(i, preValue));
										}
									}
									if (d.data.equals(DoorStatusAlgorithm.DOOR_STATUS_OPEN)) {
										value = 1f;
									} else {
										value = 0f;
									}
									if (d.synced == DataRecordedResource.STATE_NOT_SYNCED) {
										points.add(new Point(time, value));
									}
									preTime = time;
									preValue = value;
								} catch (ParseException e) {
									e.printStackTrace();
								}
							} while (c.moveToNext());
						}
						c.close();
					}
					new Thread(new AddCloudDataRunnable(device.code, DoorStatusAlgorithm.CHANNEL_NAME, points)).start();
					// update to synced
					where = TableDataRecorded.ADDRESS + "='" + device.address + "' AND " + TableDataRecorded.SYNCED
							+ "<>1";
					ContentValues values = new ContentValues();
					values.put(TableDataRecorded.SYNCED, DataRecordedResource.STATE_SYNCED);
					getContentResolver().update(TableDataRecorded.CONTENT_URI, values, where, null);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	private class AddCloudDataRunnable implements Runnable {
		private String sensorName;
		private String channelName;
		private ArrayList<Point> points;

		public AddCloudDataRunnable(String sensorName, String channelName, ArrayList<Point> points) {
			this.sensorName = sensorName;
			this.channelName = channelName;
			this.points = points;
		}

		@Override
		public void run() {
			synchronized (cloudDevice) {
				try {
					if (points == null || points.size() == 0)
						return;
					cloudDevice.authenticate();
					Log.d("SensorCloud", "authenticate for put data size " + points.size());
					if (!cloudDevice.doesSensorExist(sensorName)) {
						Log.d("SensorCloud", "adding Sensor " + sensorName);
						cloudDevice.addSensor(sensorName);
						Log.d("SensorCloud", "Sensor " + sensorName + " added");
					}
					if (!cloudDevice.doesChannelExist(sensorName, channelName)) {
						Log.d("SensorCloud", "adding Channel " + sensorName + ":" + channelName);
						cloudDevice.addChannel(sensorName, channelName);
						Log.d("SensorCloud", "Channel " + channelName + " added");
					}
					cloudDevice.addTimeseriesData(sensorName, channelName, SampleRate.Seconds(Consts.SAMPLE_RATE),
							points);
					Log.d("SensorCloud", "addTimeseriesData size:" + points.size() + " successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class RemoveSensorRunnable implements Runnable {
		private String sensorName;

		public RemoveSensorRunnable(String sensorName) {
			this.sensorName = sensorName;
		}

		@Override
		public void run() {
			synchronized (cloudDevice) {
				try {
					cloudDevice.authenticate();
					Log.d("SensorCloud", "authenticate for remove sensor");
					cloudDevice.removeChannel(sensorName, DoorStatusAlgorithm.CHANNEL_NAME);
					Log.d("SensorCloud", "Remove channel successfull");
					cloudDevice.removeSensor(sensorName);
					Log.d("SensorCloud", "Remove sensor successfull");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}