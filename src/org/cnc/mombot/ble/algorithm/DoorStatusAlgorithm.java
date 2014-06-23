package org.cnc.mombot.ble.algorithm;

import java.util.HashMap;

import org.ble.sensortag.sensor.TiMagnetometerSensor;
import org.ble.sensortag.sensor.TiRangeSensors;
import org.ble.sensortag.sensor.TiSensors;
import org.cnc.mombot.utils.SharePrefs;

/**
 * The algorithm to detect door status
 * 
 * @author Thành
 * 
 */
public class DoorStatusAlgorithm extends SensorAlgorithm {
	public static final HashMap<String, Float> mDoorCloseValue = new HashMap<String, Float>();
	public static final String DOOR_STATUS_OPEN = "door opened";
	public static final String DOOR_STATUS_CLOSE = "door closed";
	public static final String CHANNEL_NAME = "door_status";
	/**
	 * Compass values
	 */
	private float[] magnitudeValues = new float[3];

	/**
	 * Initialises a new DoorStatusAlgorithm
	 * 
	 * @param sensorManager
	 *            The android sensor manager
	 */
	public DoorStatusAlgorithm(SensorAlgorithmInterface service) {
		super(service);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void enableSensor(String deviceAddress) {
		super.enableSensor(deviceAddress);

		// enable sensorMagnetometer
		TiRangeSensors<float[], Float> sensorMagnetometer = (TiRangeSensors<float[], Float>) TiSensors
				.getSensor(TiMagnetometerSensor.UUID_SERVICE);
		sensorMagnetometer.setPeriod(SENSOR_DURATION);
		service.enableSensor(deviceAddress, sensorMagnetometer, true);
		service.updateSensor(deviceAddress, sensorMagnetometer);
	}

	/**
	 * delta for door close status
	 */
	private static final float delta = 2f;

	@Override
	public void update(String deviceAddress, String sensorUUID, float[] values) {
		super.update(deviceAddress, sensorUUID, values);
		// we received a sensor event. it is a good practice to check
		// that we received the proper event
		if (sensorUUID.equals(TiMagnetometerSensor.UUID_SERVICE)) {
			magnitudeValues = DataFilter.lowPass(values, magnitudeValues);
			float absoluteValue = values[1];
			// we check having close door value
			float doorCloseValue = 0;
			// first: we get door close value from memory hash map
			// if memory hasn't value, we get it from saved prefs
			if (mDoorCloseValue.containsKey(deviceAddress)) {
				doorCloseValue = mDoorCloseValue.get(deviceAddress);
			} else {
				doorCloseValue = SharePrefs.getInstance().getDoorCloseValue(deviceAddress);
				// if value still 0, we consider first value is door close value, save to pref
				if (doorCloseValue == 0) {
					doorCloseValue = absoluteValue;
					SharePrefs.getInstance().saveDoorCloseValue(deviceAddress, doorCloseValue);
				}
				mDoorCloseValue.put(deviceAddress, doorCloseValue);
			}
			if (doorCloseValue == 0)
				return;
			if (absoluteValue > doorCloseValue - delta && absoluteValue < doorCloseValue + delta) {
				// absolute value in door close range with delta
				service.onSaveLog(deviceAddress, DOOR_STATUS_CLOSE);
			} else {
				service.onSaveLog(deviceAddress, DOOR_STATUS_OPEN);
			}
		}
	}

	@Override
	public void onDeviceDisconnect(String deviceAddress) {
		super.onDeviceDisconnect(deviceAddress);
		mDoorCloseValue.remove(deviceAddress);
	}
}
