package org.ble.sensortag.config;

public interface AppConfig {
    public static final boolean DEBUG = false;
    /**
     * Indicates whether BleService is used as remote service.<br/>
     * It would send broadcast messages in this case.
     */
    public static final boolean REMOTE_BLE_SERVICE = false;
    /**
     * Indicates whether {@link org.ble.sensortag.BleSensorsRecordService} would be enabled.
     */
    public static final boolean ENABLE_RECORD_SERVICE = true;
    /**
     * Enables magnet sensor to be used while sensor fusion calculation.
     */
    public static final boolean SENSOR_FUSION_USE_MAGNET_SENSOR = true;

    public static final String BLE_DEVICE_NAME = "SensorTag";
}
