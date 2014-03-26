package org.cnc.msrobot.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cnc.msrobot.R;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

	private static final String TIME_QUARTER_TO = "QUARTER TO";
	private static final String TIME_QUARTER_PAST = "QUARTER PAST";
	private static final String TIME_HALF_PAST = "HALF PAST";
	private static final String TIME_OCLOCK = "O'CLOCK";
	private static final String TIME_PAST = "PAST";
	private static final String TIME_TO = "TO";

	public static Date recognizeTime(String timeString) {
		Pattern patternNumber = Pattern.compile("-?\\d+");
		// get hour, minute and set second
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		calendar.set(Calendar.SECOND, 0);
		// hh:45
		timeString = timeString.toUpperCase(Locale.US);
		Matcher m = patternNumber.matcher(timeString);
		if (timeString.contains(TIME_QUARTER_TO)) {
			// find first number
			if (m.find()) {
				hour = Integer.parseInt(m.group());
				minute = 45;
			} else {
				// not found any number, return null
				return null;
			}
		} else if (timeString.contains(TIME_QUARTER_PAST)) {
			// find first number
			if (m.find()) {
				hour = Integer.parseInt(m.group());
				minute = 15;
			} else {
				// not found any number, return null
				return null;
			}
		} else if (timeString.contains(TIME_HALF_PAST)) {
			// find first number
			if (m.find()) {
				hour = Integer.parseInt(m.group());
				minute = 30;
			} else {
				// not found any number, return null
				return null;
			}
		} else if (timeString.contains(TIME_OCLOCK)) {
			// find first number
			if (m.find()) {
				hour = Integer.parseInt(m.group());
				minute = 0;
			} else {
				// not found any number, return null
				return null;
			}
		} else if (timeString.contains(TIME_PAST)) {
			// find first number
			if (m.find()) {
				minute = Integer.parseInt(m.group());
				// find second number
				if (m.find()) {
					hour = Integer.parseInt(m.group());
				} else {
					return null;
				}
			} else {
				// not found any number, return null
				return null;
			}
		} else if (timeString.contains(TIME_TO)) {
			// find first number
			if (m.find()) {
				minute = 60 - Integer.parseInt(m.group());
				// find second number
				if (m.find()) {
					hour = Integer.parseInt(m.group());
				} else {
					return null;
				}
			} else {
				// not found any number, return null
				return null;
			}
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * @return volume indicator that shows the average volume of the last read buffer
	 */
	public static float getRmsdb(byte[] buff) {
		long sumOfSquares = getRms(buff, buff.length, buff.length);
		double rootMeanSquare = Math.sqrt(sumOfSquares / (buff.length / 2));
		if (rootMeanSquare > 1) {
			Logger.info("zzz", "getRmsdb(): " + rootMeanSquare);
			// why 10? (biet chet lien)
			return (float) (10 * Math.log10(rootMeanSquare));
		}
		return 0;
	}

	private static long getRms(byte[] buff, int end, int span) {
		int begin = end - span;
		if (begin < 0) {
			begin = 0;
		}
		// make sure begin is even
		if (0 != (begin % 2)) {
			begin++;
		}

		long sum = 0;
		for (int i = begin; i < end; i += 2) {
			// TODO: We don't need the whole short, just take the 2nd byte (the more significant one)
			// byte curSample = mCurrentRecording[i+1];

			short curSample = getShort(buff[i], buff[i + 1]);
			sum += curSample * curSample;
		}
		return sum;
	}

	/*
	 * <p>Converts two bytes to a short, assuming that the 2nd byte is more significant (LITTLE_ENDIAN format).</p>
	 * 
	 * <pre> 255 | (255 << 8) 65535 </pre>
	 */
	private static short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getRealPathFromURI(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) { return Environment.getExternalStorageDirectory() + "/"
						+ split[1]; }

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) { return uri.getPath(); }

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other file-based
	 * ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null) cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
