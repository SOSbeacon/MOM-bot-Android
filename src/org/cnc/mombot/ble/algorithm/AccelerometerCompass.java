package org.cnc.mombot.ble.algorithm;

import org.ble.sensortag.sensor.TiAccelerometerSensor;
import org.ble.sensortag.sensor.TiGyroscopeSensor;
import org.ble.sensortag.sensor.TiMagnetometerSensor;
import org.ble.sensortag.sensor.TiRangeSensors;
import org.ble.sensortag.sensor.TiSensors;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * The orientation provider that delivers the current orientation from the {@link Sensor#TYPE_ACCELEROMETER
 * Accelerometer} and {@link Sensor#TYPE_MAGNETIC_FIELD Compass}.
 * 
 * @author Alexander Pacha
 * 
 */
public class AccelerometerCompass extends SensorAlgorithm {
	/**
	 * Compass values
	 */
	private float[] magnitudeValues = new float[3];

	/**
	 * Accelerometer values
	 */
	private float[] accelerometerValues = new float[3];

	// orientation angles from accel and magnet
	private float[] accMagOrientation = new float[3];
	/**
	 * Initialises a new AccelerometerCompassProvider
	 * 
	 * @param sensorManager
	 *            The android sensor manager
	 */
	@SuppressWarnings("unchecked")
	public AccelerometerCompass(SensorAlgorithmInterface service, String deviceAddress) {
		super(service);

		// Add the compass and the accelerometer
		TiRangeSensors<float[], Float> sensorAccelerometer = (TiRangeSensors<float[], Float>) TiSensors
				.getSensor(TiAccelerometerSensor.UUID_SERVICE);
		sensorAccelerometer.setPeriod(SENSOR_DURATION);
		service.enableSensor(deviceAddress, sensorAccelerometer, true);
		service.updateSensor(deviceAddress, sensorAccelerometer);

		// enable sensorMagnetometer
		TiRangeSensors<float[], Float> sensorMagnetometer = (TiRangeSensors<float[], Float>) TiSensors
				.getSensor(TiMagnetometerSensor.UUID_SERVICE);
		sensorMagnetometer.setPeriod(SENSOR_DURATION);
		service.enableSensor(deviceAddress, sensorMagnetometer, true);
		service.updateSensor(deviceAddress, sensorMagnetometer);
	}

	@Override
	public void update(String deviceAddress, String sensorUUID, float[] values) {
		super.update(deviceAddress, sensorUUID, values);
		// we received a sensor event. it is a good practice to check
		// that we received the proper event
		if (sensorUUID.equals(TiMagnetometerSensor.UUID_SERVICE)) {
			magnitudeValues = DataFilter.lowPass(values, magnitudeValues);
			service.onOrientation(deviceAddress, magnitudeValues);
		} else if (sensorUUID.equals(TiAccelerometerSensor.UUID_SERVICE)) {
			accelerometerValues = DataFilter.lowPass(values, accelerometerValues);
		} else if (sensorUUID.equals(TiGyroscopeSensor.UUID_SERVICE)) {
		}
		if (magnitudeValues != null && accelerometerValues != null) {
			// Fuse accelerometer with compass

			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, accelerometerValues,
					magnitudeValues);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
			}
		}
	}
}
