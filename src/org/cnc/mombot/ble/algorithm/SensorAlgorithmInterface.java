package org.cnc.mombot.ble.algorithm;

import org.ble.sensortag.sensor.TiSensor;

public interface SensorAlgorithmInterface {
	public void updateSensor(String deviceAddress, TiSensor<?> sensor);
	public void enableSensor(String deviceAddress, TiSensor<?> sensor, boolean enabled);
	public void onOrientation(String deviceAddress, float[] values);
	public void onSaveLog(String deviceAddress, String data);
}
