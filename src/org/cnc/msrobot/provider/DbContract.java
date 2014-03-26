package org.cnc.msrobot.provider;

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
}
