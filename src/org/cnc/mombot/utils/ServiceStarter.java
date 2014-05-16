package org.cnc.mombot.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// start reminder alarm service
		new AlarmReceiver().setAlarmCheckServer(context);
	}
}