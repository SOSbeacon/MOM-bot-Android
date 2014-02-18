package org.cnc.msrobot.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharePrefs {

	public static final String DEFAULT_BLANK = "";
	/** Keys for saving data to shareprefs */
	public static final String MY_FACEBOOK_TOKEN = "MyFacebookToken";
	public static final String MY_TWITTER_TOKEN = "MyTwitterToken";
	public static final String SHOW_RESULT_COUNT = "ShowResultCount";
	public static final String FIRST_TIME = "FirstTime";

	private static SharePrefs instance = new SharePrefs();
	private SharedPreferences sharedPreferences;

	public static SharePrefs getInstance() {
		return instance;
	}

	public void init(Context ctx) {
		if (sharedPreferences == null) {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		}
	}

	public void clear() {
		// clear all
		sharedPreferences.edit().clear().commit();
	}

	public void save(String key, String value) {
		sharedPreferences.edit().putString(key, value).commit();
	}

	public void save(String key, int value) {
		sharedPreferences.edit().putInt(key, value).commit();
	}

	public String get(String key, String _default) {
		return sharedPreferences.getString(key, _default);
	}

	public int get(String key, int defValue) {
		return sharedPreferences.getInt(key, defValue);
	}

	public void save(String key, boolean value) {
		sharedPreferences.edit().putBoolean(key, value).commit();
	}

	public boolean get(String key, boolean _default) {
		return sharedPreferences.getBoolean(key, _default);
	}

	public void saveTokenSocial(String key, String value) {
		sharedPreferences.edit().putString(key, value).commit();
	}

	public String getTokenSocial(String key) {
		return sharedPreferences.getString(key, DEFAULT_BLANK);
	}
}
