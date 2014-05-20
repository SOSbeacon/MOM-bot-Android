package org.ble.sensortag.ble;

public interface BleServiceListener {

	/**
	 * <b>This method is called on separate from Main thread.</b>
	 */
	public void onConnected(String deviceAddress);

	/**
	 * <b>This method is called on separate from Main thread.</b>
	 */
	public void onDisconnected(String deviceAddress);

	/**
	 * <b>This method is called on separate from Main thread.</b>
	 */
	public void onServiceDiscovered(String deviceAddress);

	/**
	 * <b>This method is called on separate from Main thread.</b>
	 */
	public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text,
			byte[] data);
}
