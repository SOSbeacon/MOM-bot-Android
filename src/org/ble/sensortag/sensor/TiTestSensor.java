package org.ble.sensortag.sensor;

import org.ble.sensortag.ble.BleGattExecutor;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by steven on 9/3/13.
 */
public class TiTestSensor extends TiSensor<Void> {

    TiTestSensor() {
        super();
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getServiceUUID() {
        return "f000aa60-0451-4000-b000-000000000000";
    }

    @Override
    public String getDataUUID() {
        return "f000aa61-0451-4000-b000-000000000000";
    }

    @Override
    public String getConfigUUID() {
        return "f000aa62-0451-4000-b000-000000000000";
    }

    @Override
    public String getDataString() {
        return "";
    }

    @Override
    public BleGattExecutor.ServiceAction[] enable(boolean enable) {
        return new BleGattExecutor.ServiceAction[0];
    }

    @Override
    public BleGattExecutor.ServiceAction notify(boolean start) {
        return BleGattExecutor.ServiceAction.NULL;
    }

    @Override
    public Void parse(BluetoothGattCharacteristic c) {
        //TODO: implement method
        return null;
    }
}
