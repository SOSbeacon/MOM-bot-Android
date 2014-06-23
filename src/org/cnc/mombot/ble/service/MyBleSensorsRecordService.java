package org.cnc.mombot.ble.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.ble.sensortag.BleService;
import org.ble.sensortag.ble.BleManager;
import org.ble.sensortag.ble.BleServiceListener;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;
import org.ble.sensortag.sensor.TiRangeSensors;
import org.ble.sensortag.sensor.TiSensor;
import org.ble.sensortag.sensor.TiSensors;
import org.ble.sensortag.sensor.TiTemperatureSensor;
import org.cnc.mombot.R;
import org.cnc.mombot.ble.activity.DeviceRecordedActivity;
import org.cnc.mombot.ble.algorithm.DoorStatusAlgorithm;
import org.cnc.mombot.ble.algorithm.SensorAlgorithm;
import org.cnc.mombot.ble.algorithm.SensorAlgorithmInterface;
import org.cnc.mombot.ble.resource.DeviceResource;
import org.cnc.mombot.provider.DbContract.TableDataRecorded;
import org.cnc.mombot.provider.DbContract.TableDevice;
import org.cnc.mombot.utils.DateTimeFormater;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class MyBleSensorsRecordService extends Service implements BleServiceListener, SensorAlgorithmInterface {
	private static final String TAG = MyBleSensorsRecordService.class.getSimpleName();
	private static final int DELAY_CHECKING = 20000; // delay for check connect, reconnect: 20s
	private static final int WHICH_CHECK_CONNECTED = 1;
	private static final int WHICH_CHECK_RECONNECT = 2;
	public static final String EXTRA_COMMAND = "extra_command";
	public static final String COMMAND_STOP = "stop";
	private static final int STATE_STARTED = 1;
	private static final int STATE_STOPED = 0;
	private static final int NOTIFICATION_ID = 1;
	private ArrayList<String> deviceConnecting;
	private ContentResolver contentResolver;
	private HashMap<String, String> mPreviousLog;
	private int mServiceState = STATE_STOPED;

	/**
	 * Algorithm object
	 */
	private SensorAlgorithm algorithm;
	private final IBinder binder = new BleSensorRecordServiceBinder();
	protected final HashMap<String, BleManager> bleManager = new HashMap<String, BleManager>();
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WHICH_CHECK_CONNECTED && msg.obj instanceof String) {
				String address = (String) msg.obj;
				synchronized (deviceConnecting) {
					if (deviceConnecting != null && deviceConnecting.contains(address)) {
						disconnect(address);
						connect(address, true, DeviceResource.STATUS_RECONNECT);
					}
				}
			} else if (msg.what == WHICH_CHECK_RECONNECT && msg.obj instanceof String) {
				String address = (String) msg.obj;
				synchronized (deviceConnecting) {
					if (deviceConnecting != null && deviceConnecting.contains(address)) {
						disconnect(address);
						changeDeviceStatus(address, DeviceResource.STATUS_ERROR);
					}
				}
			}
		}

	};

	public class BleSensorRecordServiceBinder extends Binder {

		/**
		 * API interface for disconnect device
		 * 
		 * @param deviceAddress
		 *            device address
		 */
		public void disconnectDevice(String deviceAddress) {
			disconnect(deviceAddress);
		}

		public void connectDevice(String deviceAddress) {
			// new connection
			connect(deviceAddress, false, DeviceResource.STATUS_CONNECTING);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String cmd = intent.getStringExtra(EXTRA_COMMAND);
			Log.d(TAG, "start command " + cmd);
			if (COMMAND_STOP.equals(cmd)) {
				mServiceState = STATE_STOPED;
				// disconnect all device
				disconnectAll();

				contentResolver = null;
				deviceConnecting = null;
				algorithm = null;
				mPreviousLog = null;

				stopForeground(true);
				stopSelf();
			} else {
				// Check bluetooth device enable?
				final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
				if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
					return super.onStartCommand(intent, flags, startId);
				}
				// if service stated, do nothing
				if (mServiceState == STATE_STOPED) {
					mServiceState = STATE_STARTED;
					Log.d(TAG, "Service started");

					contentResolver = getContentResolver();
					deviceConnecting = new ArrayList<String>();
					algorithm = new DoorStatusAlgorithm(this);
					mPreviousLog = new HashMap<String, String>();

					startForeground();
					connectAllDeviceSaved();
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (!AppConfig.ENABLE_RECORD_SERVICE) {
			stopSelf();
			return;
		}

		// Check bluetooth device enable?
		final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			Toast.makeText(this, R.string.dialog_enable_bluetooth, Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		}
		// check ble support
		final int bleStatus = BleUtils.getBleStatus(getBaseContext());
		switch (bleStatus) {
		case BleUtils.STATUS_BLE_NOT_AVAILABLE:
			Toast.makeText(this, R.string.dialog_error_no_ble, Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
			Toast.makeText(this, R.string.dialog_error_no_bluetooth, Toast.LENGTH_SHORT).show();
			stopSelf();
			return;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "service destroy");
		mServiceState = STATE_STOPED;
		stopForeground(true);
		disconnectAllDeviceSaved();
	}

	@Override
	public void onConnected(String deviceAddress) {
		Log.d(TAG, deviceAddress + " Connected");
		// add device to device connecting array
		synchronized (deviceConnecting) {
			deviceConnecting.add(deviceAddress);
			broadcastUpdate(deviceAddress, BleService.ACTION_GATT_CONNECTED);
			// post message delay 5s for check if sensor connected didn't success
			Message msg = handler.obtainMessage();
			msg.what = WHICH_CHECK_CONNECTED;
			msg.obj = deviceAddress;
			handler.sendMessageDelayed(msg, DELAY_CHECKING);
			// remove message checking reconnect if having
			handler.removeMessages(WHICH_CHECK_RECONNECT, deviceAddress);
		}
	}

	@Override
	public void onDisconnected(String deviceAddress) {
		broadcastUpdate(deviceAddress, BleService.ACTION_GATT_DISCONNECTED);
		changeDeviceStatus(deviceAddress, DeviceResource.STATUS_DISCONNECTED);
		algorithm.onDeviceDisconnect(deviceAddress);
		// call connect again to prepare when sensor power on, autoReconnect = true
		// check if disconnect by user remove or disconnect by device. Disconnect by device -> reconnect
		if (bleManager.containsKey(deviceAddress)) {
			Log.d(TAG, deviceAddress + " Disconnected by device. Reconnect");
			// check if device disconnect while connecting, show we connect new connection (autoReconnect = fales)
			if (deviceConnecting.contains(deviceAddress))
				connect(deviceAddress, false, DeviceResource.STATUS_CONNECTING);
			else
				connect(deviceAddress, true, DeviceResource.STATUS_CONNECTING);
			// check for reconnect success
			Message msg = handler.obtainMessage();
			msg.what = WHICH_CHECK_RECONNECT;
			msg.obj = deviceAddress;
			handler.sendMessageDelayed(msg, DELAY_CHECKING);
		} else {
			Log.d(TAG, deviceAddress + " Disconnected by user");
			// remove message checking reconnect if having
			handler.removeMessages(WHICH_CHECK_RECONNECT, deviceAddress);
		}
		// remove message check reconnect
		handler.removeMessages(WHICH_CHECK_CONNECTED, deviceAddress);

		// remove connecting device list
		deviceConnecting.remove(deviceAddress);
		// remove previous device log list
		mPreviousLog.remove(deviceAddress);
	}

	@Override
	public void onServiceDiscovered(String deviceAddress) {
		Log.d(TAG, "Service discovered");
		algorithm.enableSensor(deviceAddress);
	}

	@Override
	public void onOrientation(String deviceAddress, float[] values) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data) {
		synchronized (deviceConnecting) {
			if (deviceConnecting.contains(deviceAddress)) {
				// change device status to connected when receiver first data
				changeDeviceStatus(deviceAddress, DeviceResource.STATUS_CONNECTED);
				deviceConnecting.remove(deviceAddress);
				// remove message check reconnect
				handler.removeMessages(WHICH_CHECK_CONNECTED, deviceAddress);
			}
			final TiRangeSensors<float[], Float> sensor = (TiRangeSensors<float[], Float>) TiSensors
					.getSensor(serviceUuid);
			final float[] values = sensor.getData();
			// calibrate(values);
			algorithm.update(deviceAddress, serviceUuid, values);
		}
	}

	public void connect(String address, boolean autoReconnect, int status) {
		// change status to connecting
		changeDeviceStatus(address, status);
		connect(address, autoReconnect);
	}

	@Override
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
	 * @param autoReconnect
	 *            auto reconnect
	 */
	private void connect(String address, boolean autoReconnect) {
		Log.d(TAG, "Connect device: " + address);
		if (bleManager.containsKey(address)) {
			getBleManager(address).connect(getBaseContext(), address, autoReconnect);
		} else {
			BleManager ble = new BleManager();
			if (ble.initialize(getBaseContext())) {
				ble.setServiceListener(this);
				ble.connect(getBaseContext(), address, autoReconnect);
				bleManager.put(address, ble);
			}
		}
	}

	/**
	 * manual disconnect all device, call when bluetooth state turn off
	 */
	private void disconnectAll() {
		String deviceAddress[] = bleManager.keySet().toArray(new String[bleManager.size()]);
		for (String address : deviceAddress) {
			disconnect(address);
			broadcastUpdate(address, BleService.ACTION_GATT_DISCONNECTED);
			changeDeviceStatus(address, DeviceResource.STATUS_DISCONNECTED);
		}
	}

	/**
	 * disconnect one device by device address
	 * 
	 * @param address
	 *            device address
	 */
	private void disconnect(String address) {
		if (bleManager.containsKey(address)) {
			BleManager ble = getBleManager(address);
			Log.d(TAG, "Disconnect device: " + address);
			// disconnect by user, remove ble manager
			bleManager.remove(address);
			ble.disconnect();
			ble.close();
			// remove connecting device list
			deviceConnecting.remove(address);
			// remove previous device log list
			mPreviousLog.remove(address);
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
	private BleManager getBleManager(String address) {
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
	@Override
	public void enableSensor(String deviceAddress, TiSensor<?> sensor, boolean enabled) {
		BleManager ble = bleManager.get(deviceAddress);
		if (ble != null) {
			Log.d(TAG, "Enable sensor for " + deviceAddress);
			ble.enableSensor(sensor, enabled);
		}
	}

	private void disconnectAllDeviceSaved() {
		if (contentResolver == null)
			return;
		if (deviceConnecting != null)
			deviceConnecting.clear();
		synchronized (contentResolver) {
			Cursor c = contentResolver.query(TableDevice.CONTENT_URI, null, null, null, null);
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						DeviceResource d = new DeviceResource(c);
						disconnect(d.address);
					} while (c.moveToNext());
				}
				c.close();
			}
		}
		changeAllDeviceStatus(DeviceResource.STATUS_DISCONNECTED);
	}

	private void startForeground() {
		Intent intent = new Intent(getApplicationContext(), DeviceRecordedActivity.class);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setContentText("MOM-BOT monitoring...");
		builder.setContentTitle("MOM-BOT");
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(pi);
		builder.setAutoCancel(false);
		builder.setOnlyAlertOnce(true);
		builder.setOngoing(true);
		startForeground(NOTIFICATION_ID, builder.build());
	}

	private void connectAllDeviceSaved() {
		disconnectAllDeviceSaved();
		synchronized (contentResolver) {
			// Get Device is register for recording in database
			Cursor c = contentResolver.query(TableDevice.CONTENT_URI, null, null, null, null);
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						DeviceResource d = new DeviceResource(c);
						if (!deviceConnecting.contains(d.address)) {
							Log.d(TAG, "Connecting device: " + d.address + " - " + d.code);
							connect(d.address, false, DeviceResource.STATUS_CONNECTING);
						}
					} while (c.moveToNext());
				}
				c.close();
			}
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
			Log.d(TAG, "change device " + deviceAddress + " status " + status);
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDevice.STATUS, status);
			String where = TableDevice.ADDRESS + "='" + deviceAddress + "'";
			contentResolver.update(TableDevice.CONTENT_URI, value, where, null);
			contentResolver.notifyChange(TableDevice.CONTENT_URI, null);
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
			contentResolver.notifyChange(TableDevice.CONTENT_URI, null);
		}
	}

	@Override
	public void onSaveLog(String deviceAddress, String data) {
		synchronized (contentResolver) {
			// check for previous log
			String preData = mPreviousLog.get(deviceAddress);
			if (data.equals(preData))
				return;
			// change status of device
			ContentValues value = new ContentValues();
			value.put(TableDataRecorded.ADDRESS, deviceAddress);
			value.put(TableDataRecorded.SERVICE_UUID, TiTemperatureSensor.UUID_SERVICE);
			value.put(TableDataRecorded.DATA, data);
			value.put(TableDataRecorded.SYNCED, 0);
			value.put(TableDataRecorded.TIME_SAVED, DateTimeFormater.timeServerFormat.format(new Date()));
			contentResolver.insert(TableDataRecorded.CONTENT_URI, value);
			mPreviousLog.put(deviceAddress, data);
			contentResolver.notifyChange(TableDataRecorded.CONTENT_URI, null);
		}
	}

	private void broadcastUpdate(final String deviceAddress, final String action) {
		final Intent intent = new Intent(action);
		intent.putExtra(BleService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		sendBroadcast(intent);
	}
}
