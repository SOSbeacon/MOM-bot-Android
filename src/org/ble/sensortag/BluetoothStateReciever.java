package org.ble.sensortag;

import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothStateReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AppConfig.ENABLE_RECORD_SERVICE)
            return;

        final BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(context);
        final Intent gattServiceIntent = new Intent(context, BleSensorsRecordService.class);
        if (adapter != null && adapter.isEnabled()) {
            context.startService(gattServiceIntent);
        } else {
            context.stopService(gattServiceIntent);
        }
    }
}