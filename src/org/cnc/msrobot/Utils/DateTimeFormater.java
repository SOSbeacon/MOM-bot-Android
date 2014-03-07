package org.cnc.msrobot.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateTimeFormater {
	public static final SimpleDateFormat timeServerFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
	public static final SimpleDateFormat timeFullFormater = new SimpleDateFormat("HH:mm:ss", Locale.US);
	public static final SimpleDateFormat dateFullFormater = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US);
	public static final SimpleDateFormat timeFormater = new SimpleDateFormat("KK:mm aa", Locale.US);
	public static final SimpleDateFormat compareFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
}
