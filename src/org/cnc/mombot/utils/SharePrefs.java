package org.cnc.mombot.utils;

import org.cnc.mombot.resource.WeatherResource;
import org.cnc.mombot.resource.weather.WeatherCondition;
import org.cnc.mombot.resource.weather.WeatherTemperature;
import org.cnc.mombot.resource.weather.WeatherWind;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class SharePrefs {

	public static final String DEFAULT_BLANK = "";
	/** Keys for saving data to shareprefs */
	public static final String CURRENT_LOCATION = "current_location";
	public static final String CURRENT_WEATHER = "current_weather";
	public static final String PREF_RECOGNIZE_SERVICE_PACKAGE = "recognize_service_package";
	public static final String PREF_RECOGNIZE_SERVICE_CLASS = "recognize_service_class";
	private static final String PREF_GMAIL_USERNAME = "gmail_username";
	private static final String PREF_GMAIL_PASS = "gmail_pass";
	private static final String PREF_LOGIN_TOKEN = "login_token";
	private static final String PREF_CHECK_TTS = "check_tts";
	private static final String PREF_CURRENT_EMERGENCY_ID = "emergency_id";

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

	public Location getCurrentLocation() {
		Location l = new Location("MOM-BOT");
		Double lng = Double.parseDouble(sharedPreferences.getString(CURRENT_LOCATION + ".lng", DEFAULT_BLANK));
		Double lat = Double.parseDouble(sharedPreferences.getString(CURRENT_LOCATION + ".lat", DEFAULT_BLANK));
		l.setLongitude(lng);
		l.setLatitude(lat);
		return l;
	}

	public void saveCurrentLocation(Location location) {
		sharedPreferences.edit().putString(CURRENT_LOCATION + ".lng", location.getLongitude() + "").commit();
		sharedPreferences.edit().putString(CURRENT_LOCATION + ".lat", location.getLatitude() + "").commit();
	}

	public WeatherResource getCurrentWeather() {
		String main = sharedPreferences.getString(CURRENT_WEATHER + ".condition.main", DEFAULT_BLANK);
		String description = sharedPreferences.getString(CURRENT_WEATHER + ".condition.description", DEFAULT_BLANK);
		String icon = sharedPreferences.getString(CURRENT_WEATHER + ".condition.icon", DEFAULT_BLANK);
		WeatherCondition weather = new WeatherCondition(main, description, icon);

		float temp = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.temp", 0);
		float tempMax = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.tempMax", 0);
		float tempMin = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.tempMin", 0);
		float grndLevel = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.grndLevel", 0);
		float humidity = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.humidity", 0);
		float pressure = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.pressure", 0);
		float seaLevel = sharedPreferences.getFloat(CURRENT_WEATHER + ".temperature.seaLevel", 0);
		WeatherTemperature temperature = new WeatherTemperature(temp, tempMin, tempMax, pressure, seaLevel, grndLevel,
				humidity);

		float speed = sharedPreferences.getFloat(CURRENT_WEATHER + ".wind.speed", 0);
		float deg = sharedPreferences.getFloat(CURRENT_WEATHER + ".wind.deg", 0);
		WeatherWind wind = new WeatherWind(speed, deg);

		return new WeatherResource(weather, temperature, wind);
	}

	public void saveCurrentWeather(WeatherResource weahter) {
		if (weahter.condition.size() > 0) {
			sharedPreferences.edit().putString(CURRENT_WEATHER + ".condition.main", weahter.condition.get(0).main)
					.commit();
			sharedPreferences.edit()
					.putString(CURRENT_WEATHER + ".condition.description", weahter.condition.get(0).description)
					.commit();
			sharedPreferences.edit().putString(CURRENT_WEATHER + ".condition.icon", weahter.condition.get(0).icon)
					.commit();
		}
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.temp", weahter.temperature.temp).commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.tempMax", weahter.temperature.tempMax)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.tempMin", weahter.temperature.tempMin)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.grndLevel", weahter.temperature.grndLevel)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.humidity", weahter.temperature.humidity)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.pressure", weahter.temperature.pressure)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".temperature.seaLevel", weahter.temperature.seaLevel)
				.commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".wind.speed", weahter.wind.speed).commit();
		sharedPreferences.edit().putFloat(CURRENT_WEATHER + ".wind.deg", weahter.wind.deg).commit();
	}

	public String getGmailUsername() {
		return sharedPreferences.getString(PREF_GMAIL_USERNAME, DEFAULT_BLANK);
	}

	public void saveGmailUsername(String accountName) {
		sharedPreferences.edit().putString(PREF_GMAIL_USERNAME, accountName).commit();
	}

	public String getGmailPass() {
		return sharedPreferences.getString(PREF_GMAIL_PASS, DEFAULT_BLANK);
	}

	public void saveGmailPass(String pass) {
		sharedPreferences.edit().putString(PREF_GMAIL_PASS, pass).commit();
	}

	public String getLoginToken() {
		return sharedPreferences.getString(PREF_LOGIN_TOKEN, DEFAULT_BLANK);
	}

	public void saveLoginToken(String token) {
		sharedPreferences.edit().putString(PREF_LOGIN_TOKEN, token).commit();
	}

	public Boolean getCheckTTS() {
		return sharedPreferences.getBoolean(PREF_CHECK_TTS, false);
	}

	public void saveCheckTTS(Boolean check) {
		sharedPreferences.edit().putBoolean(PREF_CHECK_TTS, check).commit();
	}

	public void saveEmergencyId(String id) {
		sharedPreferences.edit().putString(PREF_CURRENT_EMERGENCY_ID, id).commit();
	}

	public String getEmergencyId() {
		return sharedPreferences.getString(PREF_CURRENT_EMERGENCY_ID, "");
	}
}
