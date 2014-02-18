package org.cnc.msrobot.Utils;

import java.util.Calendar;

import org.cnc.msrobot.R;

import android.content.Context;

public class SpeakUtils {
	public static String formatCurrentTime(Context context) {
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
}
