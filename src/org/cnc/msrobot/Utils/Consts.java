package org.cnc.msrobot.utils;

import java.io.File;

import android.os.Environment;

public class Consts {

	public static final String PHOTO_JPG_EXTENSION = ".jpg";
	public static final String BASE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator
			+ "org.cnc.seedforminecraft" + File.separator;
	public static final String TEMP_PATH = BASE_PATH + "temp" + File.separator;
	public static final String TEMP_IMAGE_NAME = "temp_image";
	public static final String TEMP_SELECTED_IMAGE_NAME = "temp_selected_image" + PHOTO_JPG_EXTENSION;

	public static final String BASE_URL = "";

	public static final String ID_PARAM = "id";
	public static final String CITY_PARAM = "city";
	public static final String LON_PARAM = "lon";
	public static final String LAT_PARAM = "lat";

	public static final String HOLDER_ID_PARAM = "{id}";
	public static final String HOLDER_CITY_PARAM = "{city}";
	public static final String HOLDER_LON_PARAM = "{lon}";
	public static final String HOLDER_LAT_PARAM = "{lat}";

	public static final int REQUEST_CODE_LOCATION = 101;

	public class URLConsts {
		public static final String GET_WEATHER_BY_ID_INFO = "http://api.openweathermap.org/data/2.5/weather?id={id}&units=metric";
		public static final String GET_WEATHER_BY_LOCAITON_INFO = "http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric";
		public static final String SEARCH_CITY = "http://api.openweathermap.org/data/2.5/find?q={city}";
		public static final String WEATHER_ICON_URL = "http://openweathermap.org/img/w/{id}.png";
	}
}
