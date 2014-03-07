package org.cnc.msrobot.provider;

import java.util.ArrayList;
import java.util.Arrays;

import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.provider.DbHelper.Tables;
import org.cnc.msrobot.utils.Logger;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DbProvider extends ContentProvider {
	private static final String TAG = DbProvider.class.getSimpleName();
	public static final String CONTENT_AUTHORITY = "org.cnc.msrobot";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	public static final String PATH_EVENTS = "PATH_EVENTS";
	private static final int CODE_EVENTS = 1;

	private DbHelper mDbHelper;
	private UriMatcher mUriMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = CONTENT_AUTHORITY;
		/** Uri for users. */
		matcher.addURI(authority, PATH_EVENTS, CODE_EVENTS);
		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Logger.debug(TAG, "delete(uri=" + uri + ")");

		if (uri == BASE_CONTENT_URI) {
			// Handle whole database deletes (e.g. when signing out)
			deleteDatabase();
			return 1;
		}
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			final SQLiteQueryBuilder builder = buildSimpleSelection(uri);
			int retVal = db.delete(builder.getTables(), selection, selectionArgs);
			Logger.debug(TAG, "delete table: " + builder.getTables());
			return retVal;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return 0;
	}

	/**
	 * Delete database
	 */
	private void deleteDatabase() {
		// TODO: wait for content provider operations to finish, then tear down
		mDbHelper.close();
		Context context = getContext();
		DbHelper.deleteDatabase(context);
		mDbHelper = new DbHelper(getContext());
	}

	@Override
	public String getType(Uri uri) {
		final int code = mUriMatcher.match(uri);
		switch (code) {
			case CODE_EVENTS:
				return TableEvent.CONTENT_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			final SQLiteQueryBuilder builder = buildSimpleSelection(uri);
			long id = db.insertWithOnConflict(builder.getTables(), null, values, SQLiteDatabase.CONFLICT_IGNORE);
			Uri newUri = ContentUris.withAppendedId(uri, id);
			Logger.debug(TAG, "insert table: " + builder.getTables());
			return newUri;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		if (values == null || values.length <= 0) return 0;
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			final SQLiteQueryBuilder builder = buildSimpleSelection(uri);
			db.beginTransaction();
			for (ContentValues value : values) {
				if (value != null && value.size() > 0) {
					db.insertWithOnConflict(builder.getTables(), null, value, SQLiteDatabase.CONFLICT_IGNORE);
				}
			}
			db.setTransactionSuccessful();
			Logger.debug(TAG, "bulkInsert table: " + builder.getTables());
			return values.length;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return 0;
	}

	@Override
	public boolean onCreate() {
		this.mDbHelper = new DbHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Logger.debug(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// Most cases are handled with simple SQLiteQueryBuilder
		final SQLiteQueryBuilder builder = buildExpandedSelection(uri);
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			final SQLiteQueryBuilder builder = buildSimpleSelection(uri);
			int relVal = db.update(builder.getTables(), values, selection, selectionArgs);
			Logger.debug(TAG, "update table: " + builder.getTables());
			return relVal;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return 0;
	}

	/**
	 * Apply the given set of {@link ContentProviderOperation}, executing inside a {@link SQLiteDatabase} transaction.
	 * All changes will be rolled back if any single one fails.
	 */
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				ContentProviderOperation op = operations.get(i);
				results[i] = op.apply(this, results, i);
				// getContext().getContentResolver().notifyChange(op.getUri(), null);
				db.yieldIfContendedSafely();
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to support
	 * {@link #insert}, {@link #update}, and {@link #delete} operations.
	 */
	private SQLiteQueryBuilder buildSimpleSelection(Uri uri) throws UnsupportedOperationException {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		final int code = mUriMatcher.match(uri);
		switch (code) {
			case CODE_EVENTS: {
				builder.setTables(Tables.EVENTS);
				return builder;
			}
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}
	}

	/**
	 * Build an advanced {@link SQLiteQueryBuilder} to match the requested {@link Uri}. This is usually only used by
	 * {@link #query}, since it performs table joins useful for {@link Cursor} data.
	 */
	private SQLiteQueryBuilder buildExpandedSelection(Uri uri) throws UnsupportedOperationException {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		final int code = mUriMatcher.match(uri);
		switch (code) {
			case CODE_EVENTS: {
				builder.setTables(Tables.EVENTS);
				return builder;
			}
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}
	}

	/**
	 * Concatenate table name to column name
	 * 
	 * @param tableName
	 * @param column
	 *            column name
	 * @return
	 */
	public static String getQualifiedColumnName(String tableName, String column) {
		return tableName + "." + column;
	}

	/**
	 * Get the alias of the column, use in sql query to distinguish columns.
	 * 
	 * @param table
	 *            table name
	 * @param column
	 *            column name
	 * @return
	 */
	public static String getAlias(String table, String column) {
		return table + "_" + column;
	}

	// private HashMap<String, String> buildSeedTypeMapingJoinSeedColumnMap() {
	// HashMap<String, String> map = new HashMap<String, String>();
	// String seedProjection[] = { Seed._ID, Seed.AUTHOR, Seed.CATEGORY, Seed.CREATED_AT, Seed.DESCRIPTION,
	// Seed.FURNITURE, Seed.IMAGE1, Seed.IMAGE2, Seed.IMAGE3, Seed.IMAGE4, Seed.IMAGE5, Seed.RATE,
	// Seed.STATUS, Seed.TITLE, Seed.UPDATED_AT };
	// for (String col : seedProjection) {
	// String qualifiedCol = getQualifiedColumnName(Tables.SEED, col);
	// String alias = getAlias(Tables.SEED, col);
	// if (Seed._ID.equals(col)) {
	// map.put(qualifiedCol, qualifiedCol + " AS " + Seed._ID);
	// }
	// map.put(qualifiedCol, qualifiedCol + " AS " + alias);
	// }
	// String seedTypeMapingProjection[] = { SeedTypeMaping.TYPE, SeedTypeMaping.CATEGORY, SeedTypeMaping.POSITION };
	// for (String col : seedTypeMapingProjection) {
	// String qualifiedCol = getQualifiedColumnName(Tables.SEED_TYPE_MAPING, col);
	// String alias = getAlias(Tables.SEED_TYPE_MAPING, col);
	// map.put(qualifiedCol, qualifiedCol + " AS " + alias);
	// }
	// return map;
	// }
}
