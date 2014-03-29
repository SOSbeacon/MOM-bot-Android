package org.cnc.msrobot.utils;

import java.io.File;

import android.os.Environment;

public class Consts {

	public static final String APP_FOLDER_CACHE = "MsRobot/Cache";
	public static final String PHOTO_JPG_EXTENSION = ".jpg";
	public static final String BASE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator
			+ "org.cnc.seedforminecraft" + File.separator;
	public static final String TEMP_PATH = BASE_PATH + "temp" + File.separator;
	public static final String TEMP_IMAGE_NAME = "temp_image";
	public static final String TEMP_SELECTED_IMAGE_NAME = "temp_selected_image" + PHOTO_JPG_EXTENSION;
	private static final String ROOT_URL = "http://ms-robot.herokuapp.com/";
	public static final String BASE_URL = "";
	public static final int TIME_CHECK_REMINDER = 1000 * 60 * 30; // 30 minute

	public class EventType {
		public static final String TYPE_NOREPEAT = "norepeat";
		public static final String TYPE_DAILY = "daily";
		public static final String TYPE_WEEKLY = "weekly";
		public static final String TYPE_MONTHLY = "monthly";
	}

	public static final int REQUEST_GALLERY = 0;
	public static final int REQUEST_CAPTURE = 1;
	public static final int REQUEST_CROP_AFTER_CAPTURE = 2;

	public static final String PARAMS_ID = "id";
	public static final String PARAMS_CITY = "city";
	public static final String PARAMS_LON = "lon";
	public static final String PARAMS_LAT = "lat";

	public static final String HOLDER_ID_PARAM = "{id}";
	public static final String HOLDER_CITY_PARAM = "{city}";
	public static final String HOLDER_LON_PARAM = "{lon}";
	public static final String HOLDER_LAT_PARAM = "{lat}";

	public static final String PARAMS_QUERY_START = "query_start";
	public static final String PARAMS_QUERY_END = "query_end";
	public static final String PARAMS_USER_EMAIL = "user[email]";
	public static final String PARAMS_USER_PASSWORD = "user[password]";
	public static final String PARAMS_AUTH_TOKEN = "auth_token";
	public static final String PARAMS_EVENT_TITLE = "event[title]";
	public static final String PARAMS_EVENT_CONTENT = "event[content]";
	public static final String PARAMS_EVENT_TYPE = "event[type_event]";
	public static final String PARAMS_EVENT_DAY_WEEK = "event[days_of_week]";
	public static final String PARAMS_EVENT_START_TIME = "event[start_time]";
	public static final String PARAMS_EVENT_END_TIME = "event[end_time]";
	public static final String PARAMS_EVENT_END_DATE = "event[end_date]";
	public static final String PARAMS_EVENT_REPEAT = "event[repeat_interval]";

	public static final String HOLDER_QUERY_START = "{query_start}";
	public static final String HOLDER_QUERY_END = "{query_end}";
	public static final String HOLDER_AUTH_TOKEN = "{auth_token}";
	public static final int REQUEST_CODE_LOCATION = 101;

	public class URLConsts {
		public static final String GET_WEATHER_BY_ID_INFO = "http://api.openweathermap.org/data/2.5/weather?id={id}&units=metric";
		public static final String GET_WEATHER_BY_LOCAITON_INFO = "http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric";
		public static final String SEARCH_CITY = "http://api.openweathermap.org/data/2.5/find?q={city}";
		public static final String WEATHER_ICON_URL = "http://openweathermap.org/img/w/{id}.png";
		public static final String LOGIN_URL = ROOT_URL + "users/sign_in.json";
		public static final String LOGOUT_URL = ROOT_URL + "users/sign_out.json";
		public static final String GET_LIST_EVENT_URL = ROOT_URL
				+ "event/list_event_on_range_date.json?query_start={query_start}&query_end={query_end}&auth_token={auth_token}";
		public static final String CREATE_EVENT_URL = ROOT_URL + "events.json";
		public static final String DELETE_EVENT_URL = ROOT_URL + "event/{id}.json";
		public static final String EDIT_EVENT_URL = ROOT_URL + "event/{id}.json";
		public static final String GET_LIST_CONTACT = ROOT_URL + "group_contacts.json?auth_token={auth_token}";
	}

	public class RequestCode {
		public final static int REQUEST_ADD_OR_EDIT_EVENT = 1;
		public static final int REQUEST_CODE_CHECK_TTS = 2;
		public static final int REQUEST_EMAIL_SETUP = 3;
		public static final int REQUEST_RECOGNIZE = 4;

	}
}
