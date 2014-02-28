package org.cnc.msrobot.utils;

import java.util.Calendar;

import org.cnc.msrobot.R;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class AppUtils {
	public static int TYPE_WIFI = 1;
	public static int TYPE_MOBILE = 2;
	public static int TYPE_NOT_CONNECTED = 0;

	/**
	 * Get connectivity status
	 * 
	 * @param context
	 * @return TYPE_WIFI, TYPE_MOBILE, TYPE_NOT_CONNECTED
	 */
	public static int getConnectivityStatus(final Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork && activeNetwork.isConnectedOrConnecting()) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI;

			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE;
		}
		return TYPE_NOT_CONNECTED;
	}

	public static void hideKeyBoard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/*
	 * Get screen size
	 * 
	 * @param context
	 * 
	 * @return array size 2. index 0 is screen width, index 1 is screen height
	 */
	public static int[] getScreenSize(Context context) {
		int[] size = new int[2];
		if (context != null) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displayMetrics);
			size[0] = displayMetrics.widthPixels;
			size[1] = displayMetrics.heightPixels;
		}
		return size;
	}

	/**
	 * return current time format for speech
	 * 
	 * @param context
	 * @return
	 */
	public static String getCurrentTimeForSpeech(Context context) {
		String am = context.getResources().getString(R.string.Time_AM);
		String pm = context.getResources().getString(R.string.Time_PM);
		String inAfternoon = context.getResources().getString(R.string.Time_InAfternoon);
		String inEvening = context.getResources().getString(R.string.Time_InEvening);
		String inMorning = context.getResources().getString(R.string.Time_InMorning);
		String verboseTimeHour = context.getResources().getString(R.string.Verbose_Time_Hour);
		String verboseTime = context.getResources().getString(R.string.Verbose_Time);

		StringBuffer buf = new StringBuffer();

		Calendar c = Calendar.getInstance();

		int hour = c.get(Calendar.HOUR);
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);

		if (hour == 0) hour = 12;

		String amPm = c.get(Calendar.AM_PM) == Calendar.AM ? am : pm;

		String daySegment = c.get(Calendar.AM_PM) == Calendar.AM ? inMorning
				: (hourOfDay > 11 && hourOfDay < 18) ? inAfternoon : inEvening;

		String[] t = { "" + hour, "" + min, daySegment, amPm, "" + hourOfDay };

		if (min == 0) {
			buf.append(String.format(verboseTimeHour, (Object[]) t));
		} else {
			buf.append(String.format(verboseTime, (Object[]) t));
		}

		return buf.toString();
	}

	public static void showSentSmsIntent(Context context, String body) {
		String uri = "smsto:";
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
		intent.putExtra("sms_body", body);
		intent.putExtra("compose_mode", true);
		context.startActivity(intent);
	}

	public static void showSentEmailIntent(Context context, String subject, String body) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		context.startActivity(emailIntent);
	}

	public static void showGoogleSearchIntent(Context context, String query) {
		Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		intent.putExtra(SearchManager.QUERY, query); // query contains search string
		context.startActivity(intent);
	}
}
