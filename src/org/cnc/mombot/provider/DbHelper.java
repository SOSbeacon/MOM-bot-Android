package org.cnc.mombot.provider;

import org.cnc.mombot.provider.DbContract.TableContact;
import org.cnc.mombot.provider.DbContract.TableEvent;
import org.cnc.mombot.provider.DbContract.TableGroupContact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public final String TAG = "DbHelper";
	public static final String DATABASE_NAME = "msrobot.sqlite";
	private static final int DATABASE_VERSION = 1;
	public static final String ALTER_TABLE_SYNTAX = "ALTER TABLE ";
	public static final String ADD_COLUMN_SYNTAX = " ADD COLUMN ";
	public static final String TEXT_DATA = " TEXT";
	public static final String INTEGER_DATA = " INTEGER";
	public static final String REAL_DATA = " REAL";

	public interface Tables {
		public static final String EVENTS = "events";
		public static final String CONTACT = "contact";
		public static final String GROUP_CONTACT = "group_contact";

	}

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sqlBuilder = new StringBuilder();
		String sql = null;

		// CREATE EVENTS TABLE
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + Tables.EVENTS + " (");
		sqlBuilder.append(TableEvent._ID + " INTEGER, ");
		sqlBuilder.append(TableEvent.CONTENT + " TEXT, ");
		sqlBuilder.append(TableEvent.TITLE + " TEXT, ");
		sqlBuilder.append(TableEvent.START + " TEXT, ");
		sqlBuilder.append(TableEvent.END + " TEXT ");
		sqlBuilder.append(")");
		sql = sqlBuilder.toString();
		db.execSQL(sql);

		// CREATE CONTACT TABLE
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + Tables.CONTACT + " (");
		sqlBuilder.append(TableContact._ID + " INTEGER PRIMARY KEY, ");
		sqlBuilder.append(TableContact.NAME + " TEXT, ");
		sqlBuilder.append(TableContact.EMAIL + " TEXT, ");
		sqlBuilder.append(TableContact.MOBILE + " TEXT, ");
		sqlBuilder.append(TableContact.GROUP_ID + " INTEGER ");
		sqlBuilder.append(")");
		sql = sqlBuilder.toString();
		db.execSQL(sql);

		// CREATE GROUP CONTACT TABLE
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + Tables.GROUP_CONTACT + " (");
		sqlBuilder.append(TableGroupContact._ID + " INTEGER PRIMARY KEY, ");
		sqlBuilder.append(TableGroupContact.NAME + " TEXT ");
		sqlBuilder.append(")");
		sql = sqlBuilder.toString();
		db.execSQL(sql);
	}

	public static void deleteDatabase(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade database.
		if (newVersion > oldVersion) {
			db.beginTransaction();
			for (int i = oldVersion; i < newVersion; ++i) {
				int nextVersion = i + 1;
				switch (nextVersion) {
					case 2:
						break;
					default:
						break;
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

}
