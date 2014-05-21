package org.cnc.mombot.ble.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.ble.sensortag.BleService;
import org.ble.sensortag.ble.BleDevicesScanner;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;
import org.ble.sensortag.sensor.TiSensor;
import org.ble.sensortag.sensor.TiSensors;
import org.ble.sensortag.sensor.TiTemperatureSensor;
import org.cnc.mombot.R;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.provider.DbContract.TableDataRecorded;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.json.JSONObject;

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
	public static final String BROADCAST_FILTER = "ble_service_broadcast_filter";
	public static final String ACTION_DEVICE_CONNECT = "action_device_connect";
	public static final String ACTION_DEVICE_DISCONNECT = "action_device_disconnect";
	protected static final long SCAN_PERIOD = 10000;
	protected static final long DATA_TEMPERATURE_PERIOD = 60000;
	protected static final long DATA_ACCEL_PERIOD = 60000;

	// private final TiSensor<?> sensorAccelerometer = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);
	private final TiSensor<?> sensorTemperature = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);
	private BleDevicesScanner scanner;
	private ArrayList<String> arrayDevice = new ArrayList<String>();
	private HashMap<String, Long> mapTiming = new HashMap<String, Long>();
	private HashMap<String, Integer> lastSaved = new HashMap<String, Integer>();
	private ContentResolver contentResolver;

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
					// Get Device is register for recording in database
					Cursor c = contentResolver.query(TableDevice.CONTENT_URI, null, null, null, null);
					if (c != null) {
						if (c.moveToFirst()) {
							arrayDevice.clear();
							do {
								DeviceResource d = new DeviceResource(c);
								arrayDevice.add(d.address);
							} while (c.moveToNext());
						}
						c.close();
					}
					Log.d(TAG, "Device discovered: " + device.getAddress());
					// check device is in register for record
					if (arrayDevice.contains(device.getAddress())) {
						connect(device.getAddress());
					}
				}
			}
		});
		scanner.setScanPeriod(SCAN_PERIOD);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (scanner == null)
			return super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "Service started");
		scanner.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Service stopped");
		if (scanner != null)
			scanner.stop();
	}

	@Override
	public void onConnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Connected");
		broadcastUpdate(deviceAddress, BleService.ACTION_GATT_CONNECTED);
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_CONNECTED);
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Disconnected");
		broadcastUpdate(deviceAddress, BleService.ACTION_GATT_DISCONNECTED);
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_DISCONNECTED);
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
		Log.d(TAG, "Service discovered");
		long now = 0;
		mapTiming.put(TiTemperatureSensor.UUID_SERVICE + deviceAddress, now);
		lastSaved.put(TiTemperatureSensor.UUID_SERVICE, 0);
		enableSensor(deviceAddress, sensorTemperature, true);
		// enableSensor(deviceAddress, sensorAccelerometer, true);
		// mapTiming.put(TiAccelerometerSensor.UUID_SERVICE + deviceAddress, now);
	}

	@Override
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
		long now = System.currentTimeMillis();
		if (TiTemperatureSensor.UUID_SERVICE.equals(serviceUuid)
				&& now - mapTiming.get(TiTemperatureSensor.UUID_SERVICE + deviceAddress) >= DATA_TEMPERATURE_PERIOD) {
			int t = 0;
			int lastTemp = lastSaved.get(TiTemperatureSensor.UUID_SERVICE);
			try {
				JSONObject json = new JSONObject(text);
				t = json.getInt("ambient");
			} catch (Exception e) {

			}
			if (t != 0 && t != lastTemp) {
				Log.d(TAG, deviceAddress + " -> new temperature data: " + t);
				saveTemperatureData(deviceAddress, t + "");
			}
			mapTiming.put(TiTemperatureSensor.UUID_SERVICE + deviceAddress, now);
		}
		// if (TiAccelerometerSensor.UUID_SERVICE.equals(serviceUuid)
		// && now - mapTiming.get(TiAccelerometerSensor.UUID_SERVICE + deviceAddress) >= DATA_ACCEL_PERIOD) {
		// mapTiming.put(TiAccelerometerSensor.UUID_SERVICE + deviceAddress, now);
		// }
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
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDevice.STATUS, status);
			String where = TableDevice.ADDRESS + "='" + deviceAddress + "'";
			contentResolver.update(TableDevice.CONTENT_URI, value, where, null);
		}
	}

	private void saveTemperatureData(String deviceAddress, String data) {
		synchronized (contentResolver) {
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDataRecorded.ADDRESS, deviceAddress);
			value.put(TableDataRecorded.SERVICE_UUID, TiTemperatureSensor.UUID_SERVICE);
			value.put(TableDataRecorded.DATA, data);
			value.put(TableDataRecorded.TIME_SAVED, new Date().toString());
			String where = TableDataRecorded.ADDRESS + "='" + deviceAddress + "'";
			contentResolver.update(TableDevice.CONTENT_URI, value, where, null);
		}
	}

	private void broadcastUpdate(final String deviceAddress, final String action) {
		final Intent intent = new Intent(action);
		intent.putExtra(BleService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		sendBroadcast(intent);
	}
}
