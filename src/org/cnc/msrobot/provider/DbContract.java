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
}
