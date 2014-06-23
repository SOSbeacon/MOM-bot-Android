/**
 * 
 */
package org.cnc.mombot.ble.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.cnc.mombot.ble.algorithm.representation.Matrixf4x4;
import org.cnc.mombot.ble.algorithm.representation.Quaternion;

import android.hardware.Sensor;

/**
 * Classes implementing this interface provide an orientation of the device either by directly accessing hardware, using
 * Android sensor fusion or fusing sensors itself.
 * 
 * The orientation can be provided as rotation matrix or quaternion.
 * 
 * 
 * @author Thanh Le ref from Alexander Pacha
 * 
 */
public abstract class SensorAlgorithm {
	protected static final int SENSOR_DURATION = 20; // 20 milisecond
	protected final SensorAlgorithmInterface service;
	/**
	 * Sync-token for syncing read/write to sensor-data from sensor manager and fusion algorithm
	 */
	protected final Object syncToken = new Object();

	/**
	 * The list of sensors used by this provider
	 */
	protected List<Sensor> sensorList = new ArrayList<Sensor>();

	/**
	 * The matrix that holds the current rotation
	 */
	protected final Matrixf4x4 currentOrientationRotationMatrix;

	/**
	 * The quaternion that holds the last rotation
	 */
	protected Quaternion lastOrientationQuaternion;

	/**
	 * The quaternion that holds the current rotation
	 */
	protected final Quaternion currentOrientationQuaternion;

	/**
	 * Initialises a new OrientationProvider
	 * 
	 * @param sensorManager
	 *            The android sensor manager
	 */
	public SensorAlgorithm(SensorAlgorithmInterface service) {
		this.service = service;

		// Initialise with identity
		currentOrientationRotationMatrix = new Matrixf4x4();

		// Initialise with identity
		currentOrientationQuaternion = new Quaternion();
	}

	/**
	 * @return Returns the current rotation of the device in the rotation matrix format (4x4 matrix)
	 */
	public Matrixf4x4 getRotationMatrix() {
		synchronized (syncToken) {
			return currentOrientationRotationMatrix;
		}
	}

	/**
	 * @return Returns the current rotation of the device in the quaternion format (vector4f)
	 */
	public Quaternion getQuaternion() {
		synchronized (syncToken) {
			return currentOrientationQuaternion.clone();
		}
	}

	public void update(String deviceAddress, String sensorUUID, float[] values) {
	}

	public void enableSensor(String deviceAddress) {
	}

	public void onDeviceDisconnect(String deviceAddress) {
	}
}
