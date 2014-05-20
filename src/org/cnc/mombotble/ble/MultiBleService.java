package org.cnc.mombotble.ble;

import java.util.HashMap;
import java.util.Map.Entry;

import org.ble.sensortag.ble.BleManager;
import org.ble.sensortag.ble.BleServiceListener;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.sensor.TiSensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Service for managing connection and data communication with a GATT server hosted on a given Bluetooth LE device.
 * Service support connect to many device
 */
public class MultiBleService extends Service implements BleServiceListener {
	private final static String TAG = MultiBleService.class.getSimpleName();

	public class LocalBinder extends Binder {
		public MultiBleService getService() {
			return MultiBleService.this;
		}
	}

	public class BleSensorRecordServiceBinder extends Binder {

		/**
		 * API interface for disconnect device
		 * 
		 * @param deviceAddress
		 *            device address
		 */
		public void disconnectDevice(String deviceAddress) {

		}
	}

	private final IBinder binder = new BleSensorRecordServiceBinder();
	private final HashMap<String, BleManager> bleManager = new HashMap<String, BleManager>();

	@Override
	public void onCreate() {
		super.onCreate();
		BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(getBaseContext());
		if (adapter == null || !adapter.isEnabled()) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			stopSelf();
			return;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		for (Entry<String, BleManager> item : bleManager.entrySet()) {
			item.getValue().close();
		}
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		for (Entry<String, BleManager> item : bleManager.entrySet()) {
			item.getValue().disconnect();
			item.getValue().close();
		}
	}

	public void updateSensor(String deviceAddress, TiSensor<?> sensor) {
		BleManager ble = bleManager.get(deviceAddress);
		if (ble != null) {
			ble.updateSensor(sensor);
		}
	}

	/**
	 * connect device address create BleManager for address
	 * 
	 * @param address
	 *            device address
	 */
	public void connect(String address) {
		if (bleManager.containsKey(address)) {
			getBleManager(address).connect(getBaseContext(), address);
		} else {
			BleManager ble = new BleManager();
			if (ble.initialize(getBaseContext())) {
				ble.setServiceListener(this);
				ble.connect(getBaseContext(), address);
				bleManager.put(address, ble);
			}
		}
	}

	/**
	 * get ble manager for device address
	 * 
	 * @author thanhle
	 * 
	 * @param address
	 *            device address
	 * @return
	 */
	public BleManager getBleManager(String address) {
		return bleManager.get(address);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @author thanhle
	 * 
	 * @param deviceAddress
	 *            device address
	 * @param sensor
	 *            sensor to be enabled/disabled
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void enableSensor(String deviceAddress, TiSensor<?> sensor, boolean enabled) {
		BleManager ble = bleManager.get(deviceAddress);
		if (ble != null) {
			ble.enableSensor(sensor, enabled);
		}
	}

	@Override
	public void onConnected(String deviceAddress) {
	}

	@Override
	public void onDisconnected(String deviceAddress) {
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
	}

	@Override
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
	}
}
