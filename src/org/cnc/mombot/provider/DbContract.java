package org.cnc.mombot.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {

	public static class TableEvent implements BaseColumns {
		public static final Uri CONTENT_URI = DbProvider.BASE_CONTENT_URI.buildUpon()
				.appendPath(DbProvider.PATH_EVENTS).build();
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.msrobot.events";

		public static final String TITLE = "title";
		public static final String CONTENT = "content";
		public static final String START = "start";
		public static final String END = "end";
	}

	public static class TableContact implements BaseColumns {
		public static final Uri CONTENT_URI = DbProvider.BASE_CONTENT_URI.buildUpon()
				.appendPath(DbProvider.PATH_CONTACT).build();
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.msrobot.contact";

		public static final String NAME = "name";
		public static final String EMAIL = "email";
		public static final String MOBILE = "mobile";
		public static final String GROUP_ID = "group_id";
	}

	public static class TableGroupContact implements BaseColumns {
		public static final Uri CONTENT_URI = DbProvider.BASE_CONTENT_URI.buildUpon()
				.appendPath(DbProvider.PATH_GROUP_CONTACT).build();
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.msrobot.group_contact";

		public static final String NAME = "name";
	}

	public static class TableDevice implements BaseColumns {
		public static final Uri CONTENT_URI = DbProvider.BASE_CONTENT_URI.buildUpon()
				.appendPath(DbProvider.PATH_DEVICES).build();
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.mombotble.devices";

		public static final String NAME = "device_name"; // name of device
		public static final String MANUFACTURER = "device_manufacturer"; // manufacturer name
		public static final String ADDRESS = "device_address"; // device address
		public static final String CODE = "device_code"; // device code assigned by user
		public static final String GROUP = "device_group"; // device group assigned by user
		public static final String LOCATION = "device_location"; // device location assigned by user
		public static final String LOCATION_TYPE = "device_location_type"; // device location type assigned by user
		public static final String STATUS = "status";
		public static final String NOTE = "device_note"; // device note assigned by user
		public static final String BATTERY_DATE = "device_battery_date"; // date when new battery installed
	}

	public static class TableDataRecorded implements BaseColumns {
		public static final Uri CONTENT_URI = DbProvider.BASE_CONTENT_URI.buildUpon()
				.appendPath(DbProvider.PATH_DATA_RECORDED).build();
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.mombotble.data_recorded";

		public static final String ADDRESS = "device_address"; // device address
		public static final String SERVICE_UUID = "service_uuid";
		public static final String DATA = "data_recorded"; 
		public static final String TIME_SAVED = "time_saved";
	}
}
