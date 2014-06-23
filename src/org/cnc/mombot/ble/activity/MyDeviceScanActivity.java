package org.cnc.mombot.ble.activity;

import java.util.ArrayList;
import java.util.Date;

import org.ble.sensortag.DeviceScanActivity;
import org.ble.sensortag.ble.BleDevicesScanner;
import org.cnc.mombot.ble.algorithm.DoorStatusAlgorithm;
import org.cnc.mombot.ble.dialogs.DeviceInformationDialog;
import org.cnc.mombot.ble.dialogs.DeviceInformationDialog.DeviceInformationDialogListener;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.ble.service.MyBleSensorsRecordService;
import org.cnc.mombot.ble.service.MyBleSensorsRecordService.BleSensorRecordServiceBinder;
import org.cnc.mombot.provider.DbContract.TableDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class MyDeviceScanActivity extends DeviceScanActivity implements DeviceInformationDialogListener {
	private ArrayList<String> deviceRecording = new ArrayList<String>();
	private BleSensorRecordServiceBinder mBinder;
	private Intent mService;

	@Override
	protected void initScanner() {
		// get list of device is recording
		Cursor cursor = getContentResolver().query(TableDevice.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				DeviceResource device = new DeviceResource(cursor);
				deviceRecording.add(device.address);
			} while (cursor.moveToNext());
		}
		scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
				Log.d("MyDeviceScanActivity", "device scan " + device.getAddress());
				// check if device not in device recording list
				if (!deviceRecording.contains(device.getAddress())) {
					leDeviceListAdapter.addDevice(device, rssi);
					leDeviceListAdapter.notifyDataSetChanged();
				}
			}
		});
		scanner.setScanPeriod(SCAN_PERIOD);
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

	private void initService() {
		// start MediaService
		mService = new Intent(this, MyBleSensorsRecordService.class);
		this.startService(mService);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// bind the service with mConnection
		if (mService == null) {
			mService = new Intent(this, MyBleSensorsRecordService.class);
		}
		this.bindService(mService, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			this.unbindService(mConnection);
		} catch (Exception ex) {
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
		DeviceInformationDialog f = (DeviceInformationDialog) getFragmentManager().findFragmentByTag(
				DeviceInformationDialog.TAG);
		Bundle bundle = new Bundle();
		bundle.putString(DeviceInformationDialog.ARG_DEVICE_NAME, device.getName());
		bundle.putString(DeviceInformationDialog.ARG_DEVICE_ADDRESS, device.getAddress());
		if (f == null) {
			f = new DeviceInformationDialog();
		}
		f.setArguments(bundle);
		f.show(getFragmentManager(), DeviceInformationDialog.TAG);
	}

	@Override
	public void onOk(String name, String address, final String code, String group, String location,
			String locationType, String note, Date batteryDate) {
		DeviceResource device = new DeviceResource(name, address, "", code, group, location, locationType, note,
				batteryDate);
		ContentValues value = device.prepareContentValue();
		getContentResolver().insert(TableDevice.CONTENT_URI, value);
		deviceRecording.add(address);
		leDeviceListAdapter.removeDevice(address);
		leDeviceListAdapter.notifyDataSetChanged();
		if (mBinder != null) {
			mBinder.connectDevice(device.address);
		}
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// try {
		// DeviceRecordedActivity.cloudDevice.authenticate();
		// DeviceRecordedActivity.cloudDevice.getListTimeseriesData(code, DoorStatusAlgorithm.CHANNEL_NAME, 0,
		// new Date().getTime());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
	}

	@Override
	public void onCancel() {
	}
}
