package org.cnc.mombot.ble.service;

import java.util.ArrayList;
import java.util.Date;

import org.ble.sensortag.BleService;
import org.ble.sensortag.ble.BleDevicesScanner;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;
import org.ble.sensortag.sensor.TiRangeSensors;
import org.ble.sensortag.sensor.TiSensors;
import org.ble.sensortag.sensor.TiTemperatureSensor;
import org.cnc.mombot.R;
import org.cnc.mombot.ble.algorithm.AccelerometerCompass;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.provider.DbContract.TableDataRecorded;
import org.cnc.mombot.provider.DbContract.TableDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class MyBleSensorsRecordService extends MultiBleService {
	private static final String TAG = MyBleSensorsRecordService.class.getSimpleName();
	public static final String COMMAND_START_SCAN = "command_device_connect";
	public static final String COMMAND_STOP_SCAN = "command_device_disconnect";
	private static final int SCAN_PERIOD = 10000;
	private BleDevicesScanner scanner;
	private ArrayList<String> arrayDevice = new ArrayList<String>();
	private ArrayList<String> deviceConnecting = new ArrayList<String>();
	private ContentResolver contentResolver;

	/**
	 * Algorithm object
	 */
	private AccelerometerCompass algorithm;

	@Override
	public void onCreate() {
		super.onCreate();
		if (!AppConfig.ENABLE_RECORD_SERVICE) {
			stopSelf();
			return;
		}

		// Check bluetooth device enable?
		final int bleStatus = BleUtils.getBleStatus(getBaseContext());
		switch (bleStatus) {
		case BleUtils.STATUS_BLE_NOT_AVAILABLE:
			Toast.makeText(getApplicationContext(), R.string.dialog_error_no_ble, Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
			Toast.makeText(getApplicationContext(), R.string.dialog_error_no_bluetooth, Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		default:
			break;
		}

		contentResolver = getContentResolver();

		// initialize scanner
		final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
		scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				synchronized (contentResolver) {
					Log.d(TAG, "Check device connecting and device connected");
					if (deviceConnecting.contains(device.getAddress()))
						return;
					// Get Device is register for recording in database
					Cursor c = contentResolver.query(TableDevice.CONTENT_URI, null, null, null, null);
					if (c != null) {
						if (c.moveToFirst()) {
							arrayDevice.clear();
							do {
								DeviceResource d = new DeviceResource(c);
								Log.d(TAG, "Device on database: " + d.address);
								arrayDevice.add(d.address);
							} while (c.moveToNext());
						}
						c.close();
					}
					// check device is in register for record and not connecting
					if (arrayDevice.contains(device.getAddress())) {
						Log.d(TAG, "Connect device: " + device.getAddress());
						deviceConnecting.add(device.getAddress());
						connect(device.getAddress());
					}
				}
			}
		});
		scanner.setScanPeriod(SCAN_PERIOD);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (scanner == null || intent == null)
			return super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "Service started");
		boolean startScan = intent.getBooleanExtra(COMMAND_START_SCAN, true);
		boolean stopScan = intent.getBooleanExtra(COMMAND_STOP_SCAN, false);
		if (stopScan) {
			scanner.stop();
		} else if (startScan) {
			scanner.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Service stopped");
		changeAllDeviceStatus(DeviceResource.STATUS_DISCONNECTED);
		if (scanner != null)
			scanner.stop();
	}

	@Override
	public void onConnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Connected");
		broadcastUpdate(deviceAddress, BleService.ACTION_GATT_CONNECTED);
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_CONNECTED);
		deviceConnecting.remove(deviceAddress);
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Disconnected");
		broadcastUpdate(deviceAddress, BleService.ACTION_GATT_DISCONNECTED);
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_DISCONNECTED);
		deviceConnecting.remove(deviceAddress);
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
		Log.d(TAG, "Service discovered");
		algorithm = new AccelerometerCompass(this, deviceAddress);
	}

	// we will draw graph after get sampling 50 times (5s)
	private static final int MAX_SAMPLING = 50;
	private int count = 0;
	double[] dataValue = new double[MAX_SAMPLING];

	@Override
	public void onOrientation(String deviceAddress, float[] values) {
		dataValue[count] = values[2];
		count++;
		if (count == MAX_SAMPLING) {
			count = 0;
			phantich(deviceAddress, dataValue);
		}
	}

	private static final double delta = 0.05d;

	private void phantich(String deviceAddress, double[] dataY) {
		// Tim diem cao nhat
		int indexMax = 0, indexMin = 0;
		double max = -10, min = 10; // min 10 because value alway < g (9.81)
		for (int i = 0; i < dataY.length; i++) {
			if (dataY[i] > max) {
				max = dataY[i];
				indexMax = i;
			}
			if (dataY[i] < min) {
				min = dataY[i];
				indexMin = i;
			}
		}
		// Neu diem cao nhat truoc diem thap nhat -> dong cua
		// Neu diem cao nhat sau diem thap nhap -> mo cua
		// Va do lech phai lon hon delta
		if (Math.abs(max - min) > delta) {
			// showCenterToast("min: " + min + ", max: " + max + ", indexMin: " + indexMin + ", indexMax: " + indexMax);
			if (indexMax < indexMin) {
				// kiem tra do lech so voi 2s truoc do
				double check = 0;
				if (indexMax > 20) {
					check = dataY[indexMax - 20];
				} else {
					check = dataY[0];
				}
				if (Math.abs(max - check) > delta / 2 && check < max) {
					// door close
					saveLogData(deviceAddress, "Door close");
				} else {
					// door open
					saveLogData(deviceAddress, "Door open");
				}
			} else {
				// door open
				saveLogData(deviceAddress, "Door open");
			}
		} else {
			// no change, not log
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
		final TiRangeSensors<float[], Float> sensor = (TiRangeSensors<float[], Float>) TiSensors.getSensor(serviceUuid);
		final float[] values = sensor.getData();
		// calibrate(values);
		algorithm.update(deviceAddress, serviceUuid, values);
	}

	private void connectAllDeviceSaved() {
		
	}
	
	/**
	 * change status of device, save to database.
	 * 
	 * @author thanhle
	 * @param deviceAddress
	 *            device address
	 * @param status
	 *            device status, consts from @link {@link DeviceResource}
	 */
	private void changeDeviceStatus(String deviceAddress, int status) {
		synchronized (contentResolver) {
			Log.d(TAG, "change device " + deviceAddress + " status " + status);
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDevice.STATUS, status);
			String where = TableDevice.ADDRESS + "='" + deviceAddress + "'";
			contentResolver.update(TableDevice.CONTENT_URI, value, where, null);
		}
	}

	/**
	 * change all device status
	 * 
	 * @param status
	 */
	private void changeAllDeviceStatus(int status) {
		synchronized (contentResolver) {
			// change status of device
			Log.d(TAG, "change all device status " + status);
			ContentValues value = new ContentValues();
			value.put(TableDevice.STATUS, status);
			contentResolver.update(TableDevice.CONTENT_URI, value, null, null);
		}
	}

	/**
	 * save sensor data to database
	 * 
	 * @param deviceAddress
	 * @param data
	 */
	public void saveLogData(String deviceAddress, String data) {
		synchronized (contentResolver) {
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDataRecorded.ADDRESS, deviceAddress);
			value.put(TableDataRecorded.SERVICE_UUID, TiTemperatureSensor.UUID_SERVICE);
			value.put(TableDataRecorded.DATA, data);
			value.put(TableDataRecorded.TIME_SAVED, new Date().toString());
			contentResolver.insert(TableDataRecorded.CONTENT_URI, value);
		}
	}

	private void broadcastUpdate(final String deviceAddress, final String action) {
		final Intent intent = new Intent(action);
		intent.putExtra(BleService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		sendBroadcast(intent);
	}
}
