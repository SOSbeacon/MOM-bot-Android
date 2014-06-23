package org.cnc.mombot.ble.service;

import org.ble.sensortag.BleService;
import org.ble.sensortag.ble.BleUtils;
import org.ble.sensortag.config.AppConfig;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MyBluetoothStateReciever extends BroadcastReceiver implements ServiceConnection {
	private BleService bleService;
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (!AppConfig.ENABLE_RECORD_SERVICE)
			return;

		final BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(context);
		final Intent gattServiceIntent = new Intent(context, MyBleSensorsRecordService.class);
		if (adapter != null && adapter.isEnabled()) {
			Log.i("MyBluetoothStateReciever", context.getPackageName() + " bluetooth state change turn on");
			context.startService(gattServiceIntent);
		} else {
			Log.i("MyBluetoothStateReciever", context.getPackageName() + " bluetooth state change turn off");
			gattServiceIntent.putExtra(MyBleSensorsRecordService.EXTRA_COMMAND, MyBleSensorsRecordService.COMMAND_STOP);
			context.startService(gattServiceIntent);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		bleService = ((BleService.LocalBinder) service).getService();
		// noinspection PointlessBooleanExpression,ConstantConditions
		if (!bleService.getBleManager().initialize(context)) {
			Log.e("MyBluetoothStateReciever", "Unable to initialize Bluetooth");
			return;
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		bleService = null;
	}
}
