package org.cnc.mombotble.ble;

import java.util.ArrayList;

import org.ble.sensortag.BleService;
import org.ble.sensortag.ble.BleDevicesScanner;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;
import org.ble.sensortag.sensor.TiAccelerometerSensor;
import org.ble.sensortag.sensor.TiSensor;
import org.ble.sensortag.sensor.TiSensors;
import org.ble.sensortag.sensor.TiTemperatureSensor;
import org.cnc.mombot.R;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombotble.resource.DeviceResource;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class MyBleSensorsRecordService extends BleService {
	private static final String TAG = MyBleSensorsRecordService.class.getSimpleName();

	private static final String RECORD_DEVICE_NAME = "SensorTag";
	protected static final long SCAN_PERIOD = 1000;

	private final TiSensor<?> sensorAccelerometer = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);
	private final TiSensor<?> sensorTemperature = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);
	private BleDevicesScanner scanner;
	private ArrayList<String> arrayDevice = new ArrayList<String>();

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

		if (!getBleManager().initialize(getBaseContext())) {
			stopSelf();
			return;
		}

		// Get Device is register for recording in database
		Cursor c = getContentResolver().query(TableDevice.CONTENT_URI, null, null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				arrayDevice.clear();
				do {
					DeviceResource device = new DeviceResource(c);
					arrayDevice.add(device.address);
				} while (c.moveToNext());
			}
			c.close();
		}

		// initialize scanner
		final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
		scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				Log.d(TAG, "Device discovered: " + device.getAddress());
				if (arrayDevice.contains(device.getAddress())) {
					scanner.stop();
					getBleManager().connect(getBaseContext(), device.getAddress());
				}
			}
		});
		scanner.setScanPeriod(SCAN_PERIOD);

		setServiceListener(this);
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
		setServiceListener(null);
		if (scanner != null)
			scanner.stop();
	}

	@Override
	public void onConnected() {
		Log.d(TAG, "Connected");
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "Disconnected");
		scanner.start();
	}

	@Override
	public void onServiceDiscovered() {
		Log.d(TAG, "Service discovered");
		enableSensor(sensorAccelerometer, true);
		enableSensor(sensorTemperature, true);
	}

	@Override
	public void onDataAvailable(String serviceUuid, String characteristicUUid, String text, byte[] data) {
		Logger.debug(TAG, "Data='" + text + "'");
	}
}
