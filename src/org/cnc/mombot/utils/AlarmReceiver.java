package org.cnc.mombot.utils;

import java.util.Calendar;

import org.cnc.mombot.activity.NotificationActivity;
import org.cnc.mombot.requestmanager.RequestManager;
import org.cnc.mombot.resource.EventResource;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;

import com.android.volley.Response.Listener;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = AlarmReceiver.class.getSimpleName();
	private static final String EXTRA_TYPE = "EXTRA_TYPE";
	private static final String EXTRA_EVENT = "EXTRA_EVENT";
	private static final int TYPE_CHECK_SERVER = 0;
	private static final int TYPE_SET_REMINDER = 1;
	private Context context;
	private Listener<EventResource[]> mRequestEventListener = new Listener<EventResource[]>() {
		@Override
		public void onResponse(EventResource[] response) {
			// cancel if response is null
			if (response == null || context == null) return;
			// task: cancel old event and create new event reminder
			cancelAlarm(context);
			for (int i = 0; i < response.length; i++) {
				setReminder(context, response[i]);
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		int type = intent.getIntExtra(EXTRA_TYPE, TYPE_CHECK_SERVER);
		Logger.debug(TAG, "onReceive: " + type);
		if (type == TYPE_CHECK_SERVER) {
			requestListEvent();
		} else if (type == TYPE_SET_REMINDER) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			@SuppressWarnings("deprecation")
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, "YOUR TAG");
			// Acquire the lock
			wl.acquire();
			// show reminder
			Intent i = new Intent(context, NotificationActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(NotificationActivity.EXTRA_CONTENT, intent.getStringExtra(EXTRA_EVENT));
			context.startActivity(i);
			// Release the lock
			wl.release();
		}
	}

	private void requestListEvent() {
		Logger.debug(TAG, "request list event");
		Bundle bundle = new Bundle();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		bundle.putLong(Consts.PARAMS_QUERY_START, calendar.getTime().getTime());
		// next 1 days
		calendar.add(Calendar.DATE, 1);
		bundle.putLong(Consts.PARAMS_QUERY_END, calendar.getTime().getTime());
		bundle.putBoolean(Consts.PARAMS_QUERY_NOT_SAVE_DB, true);
		RequestManager.getInstance().init(context);
		RequestManager.getInstance().request(Actions.ACTION_GET_LIST_EVENT, bundle, mRequestEventListener, null);
	}

	public void setAlarmCheckServer(Context context) {
		// check for login
		if (TextUtils.isEmpty(SharePrefs.getInstance().getLoginToken())) { return; }
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(EXTRA_TYPE, TYPE_CHECK_SERVER);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		// After after 10 minutes
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Consts.TIME_CHECK_REMINDER, pi);
	}

	public void cancelAlarm(Context context) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	public void setReminder(Context context, EventResource event) {
		// check if event start time smaller than now, not set alarm
		Calendar calendar = Calendar.getInstance();
		if (event.start().getTime() <= calendar.getTimeInMillis()) return;
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(EXTRA_TYPE, TYPE_SET_REMINDER);
		String json = event.toJsonString();
		Logger.debug(TAG, "set event: " + json);
		Logger.debug(TAG, "now: " + calendar.getTimeInMillis() + " alarm: " + event.start().getTime());
		intent.putExtra(EXTRA_EVENT, json);
		PendingIntent pi = PendingIntent.getBroadcast(context, event.id, intent, 0);
		am.set(AlarmManager.RTC_WAKEUP, event.start().getTime(), pi);
	}
}
