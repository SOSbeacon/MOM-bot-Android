package org.cnc.mombotble.ble;

import java.util.ArrayList;
import java.util.HashMap;

import org.ble.sensortag.ble.BleDevicesScanner;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;
import org.ble.sensortag.sensor.TiAccelerometerSensor;
import org.ble.sensortag.sensor.TiSensor;
import org.ble.sensortag.sensor.TiSensors;
import org.ble.sensortag.sensor.TiTemperatureSensor;
import org.cnc.mombot.R;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombotble.resource.DeviceResource;
import org.json.JSONException;
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

	private static final String RECORD_DEVICE_NAME = "SensorTag";
	protected static final long SCAN_PERIOD = 10000;
	protected static final long DATA_PERIOD = 1000;

	private final TiSensor<?> sensorAccelerometer = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);
	private final TiSensor<?> sensorTemperature = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);
	private BleDevicesScanner scanner;
	private ArrayList<String> arrayDevice = new ArrayList<String>();
	private HashMap<String, Long> mapTiming = new HashMap<String, Long>();
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
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_CONNECTED);
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Disconnected");
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_DISCONNECTED);
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
		Log.d(TAG, "Service discovered");
		long now = System.currentTimeMillis();
		mapTiming.put(TiAccelerometerSensor.UUID_SERVICE + deviceAddress, now);
		mapTiming.put(TiTemperatureSensor.UUID_SERVICE + deviceAddress, now);
		enableSensor(deviceAddress, sensorAccelerometer, true);
		enableSensor(deviceAddress, sensorTemperature, true);
	}

	@Override
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
		long now = System.currentTimeMillis();
		if (TiTemperatureSensor.UUID_SERVICE.equals(serviceUuid)
				&& now - mapTiming.get(TiTemperatureSensor.UUID_SERVICE + deviceAddress) >= DATA_PERIOD) {
			try {
				JSONObject json = new JSONObject(text);
				Log.d(TAG, deviceAddress + " temp ambient: " + json.getInt("ambient"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mapTiming.put(TiTemperatureSensor.UUID_SERVICE + deviceAddress, now);

		}
		if (TiAccelerometerSensor.UUID_SERVICE.equals(serviceUuid)
				&& now - mapTiming.get(TiAccelerometerSensor.UUID_SERVICE + deviceAddress) >= DATA_PERIOD) {
			mapTiming.put(TiAccelerometerSensor.UUID_SERVICE + deviceAddress, now);
		}
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
}
