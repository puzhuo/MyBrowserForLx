
package com.fujun.browser.provider;

import java.util.ArrayList;

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
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class BrowserProvider extends ContentProvider {

	// usually it is your package name + class name
	public static final String AUTHORITY = "com.fujun.browser.provider.BrowserProvider";

	/*************************** TABLE_NAVI related **********************************/
	private static final String TABLE_INDEX_NAVI_NAME = "index_navi";
	public static final String TABLE_NAVI_ID = "_id";
	public static final String TABLE_NAVI_TITLE = "title";
	public static final String TABLE_NAVI_URL = "url";
	public static final String TABLE_NAVI_REALURL = "real_url";
	public static final String TABLE_NAVI_POSITION = "position";
	public static final String TABLE_NAVI_ICON = "icon";

	public static final Uri TABLE_INDEX_NAVI_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_INDEX_NAVI_NAME);

	private static final int TABLE_INDEX_NAVI = 1;
	private static final int TABLE_INDEX_NAVI_ID = 2;

	/*************************** TABLE_FAV related **********************************/
	private static final String TABLE_FAV_NAME = "favourite";
	public static final String TABLE_FAV_ID = "_id";
	public static final String TABLE_FAV_TITLE = "title";
	public static final String TABLE_FAV_URL = "url";
	public static final String TABLE_FAV_REALURL = "real_url";
	public static final String TABLE_FAV_ICON = "icon";

	public static final Uri TABLE_FAV_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_FAV_NAME);

	private static final int TABLE_INDEX_FAV = 3;
	private static final int TABLE_INDEX_FAV_ID = 4;

	/*************************** TABLE_HIS related **********************************/
	private static final String TABLE_HIS_NAME = "history";
	public static final String TABLE_HIS_ID = "_id";
	public static final String TABLE_HIS_TITLE = "title";
	public static final String TABLE_HIS_URL = "url";
	public static final String TABLE_HIS_REALURL = "real_url";
	public static final String TABLE_HIS_ICON = "icon";
	public static final String TABLE_HIS_ACCESS_TIME = "access_time";

	public static final Uri TABLE_HIS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_HIS_NAME);

	private static final int TABLE_INDEX_HIS = 5;
	private static final int TABLE_INDEX_HIS_ID = 6;

	/*************************** TABLE_DOWNLOAD related **********************************/
	private static final String TABLE_DOWNLOAD_NAME = "download";
	public static final String TABLE_DOWNLOAD_ID = "_id";
	public static final String TABLE_DOWNLOAD_FILE_NAME = "name";
	public static final String TABLE_DOWNLOAD_MIMETYPE = "mimetype";
	public static final String TABLE_DOWNLOAD_URL = "url";
	public static final String TABLE_DOWNLOAD_FILE_SIZE = "filesize";
	public static final String TABLE_DOWNLOAD_CURRENT_SIZE = "current_size";
	public static final String TABLE_DOWNLOAD_STATUS = "status";

	public static final Uri TABLE_DOWNLOAD_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_DOWNLOAD_NAME);

	private static final int TABLE_INDEX_DOWNLOAD = 7;
	private static final int TABLE_INDEX_DOWNLOAD_ID = 8;

	private static MySQLiteOpenHelper mHelper;
	private static UriMatcher mUriMatcher = new UriMatcher(0);

	static {
		mUriMatcher.addURI(AUTHORITY, TABLE_INDEX_NAVI_NAME, TABLE_INDEX_NAVI);
		mUriMatcher.addURI(AUTHORITY, TABLE_INDEX_NAVI_NAME + "/#",
				TABLE_INDEX_NAVI_ID);
		mUriMatcher.addURI(AUTHORITY, TABLE_FAV_NAME, TABLE_INDEX_FAV);
		mUriMatcher
				.addURI(AUTHORITY, TABLE_FAV_NAME + "/#", TABLE_INDEX_FAV_ID);
		mUriMatcher.addURI(AUTHORITY, TABLE_HIS_NAME, TABLE_INDEX_HIS);
		mUriMatcher
				.addURI(AUTHORITY, TABLE_HIS_NAME + "/#", TABLE_INDEX_HIS_ID);
		mUriMatcher
				.addURI(AUTHORITY, TABLE_DOWNLOAD_NAME, TABLE_INDEX_DOWNLOAD);
		mUriMatcher.addURI(AUTHORITY, TABLE_DOWNLOAD_NAME + "/#",
				TABLE_INDEX_DOWNLOAD_ID);
	}

	@Override
	public boolean onCreate() {
		mHelper = new MySQLiteOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		long id = 0;
		switch (mUriMatcher.match(uri)) {
			case TABLE_INDEX_NAVI_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_NAVI:
				return db.query(TABLE_INDEX_NAVI_NAME, projection, selection,
						selectionArgs, null, null, sortOrder);
			case TABLE_INDEX_FAV_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_FAV:
				return db.query(TABLE_FAV_NAME, projection, selection,
						selectionArgs, null, null, sortOrder);
			case TABLE_INDEX_HIS_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_HIS:
				return db.query(TABLE_HIS_NAME, projection, selection,
						selectionArgs, null, null, sortOrder);
			case TABLE_INDEX_DOWNLOAD_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_DOWNLOAD:
				return db.query(TABLE_DOWNLOAD_NAME, projection, selection,
						selectionArgs, null, null, sortOrder);
			default:
				throw new IllegalArgumentException("illegal uri");
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		long id = 0;
		switch (mUriMatcher.match(uri)) {
			case TABLE_INDEX_NAVI:
				id = db.insert(TABLE_INDEX_NAVI_NAME, null, values);
				break;
			case TABLE_INDEX_FAV:
				id = db.insert(TABLE_FAV_NAME, null, values);
				break;
			case TABLE_INDEX_HIS:
				id = db.insert(TABLE_HIS_NAME, null, values);
				break;
			case TABLE_INDEX_DOWNLOAD:
				id = db.insert(TABLE_DOWNLOAD_NAME, null, values);
				break;
			default:
				throw new IllegalArgumentException("illegal uri");
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		long id = 0;
		int affectNum = 0;
		switch (mUriMatcher.match(uri)) {
			case TABLE_INDEX_NAVI_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_NAVI:
				affectNum = db.delete(TABLE_INDEX_NAVI_NAME, selection,
						selectionArgs);
				break;
			case TABLE_INDEX_FAV_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_FAV:
				affectNum = db.delete(TABLE_FAV_NAME, selection, selectionArgs);
				break;
			case TABLE_INDEX_HIS_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_HIS:
				affectNum = db.delete(TABLE_HIS_NAME, selection, selectionArgs);
				break;
			case TABLE_INDEX_DOWNLOAD_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_DOWNLOAD:
				affectNum = db
						.delete(TABLE_DOWNLOAD_NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("illegal uri");
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return affectNum;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		long id = 0;
		int affectNum = 0;
		switch (mUriMatcher.match(uri)) {
			case TABLE_INDEX_NAVI_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_NAVI:
				affectNum = db.update(TABLE_INDEX_NAVI_NAME, values, selection,
						selectionArgs);
				break;
			case TABLE_INDEX_FAV_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_FAV:
				affectNum = db.update(TABLE_FAV_NAME, values, selection,
						selectionArgs);
				break;
			case TABLE_INDEX_HIS_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_HIS:
				affectNum = db.update(TABLE_HIS_NAME, values, selection,
						selectionArgs);
				break;
			case TABLE_INDEX_DOWNLOAD_ID:
				id = ContentUris.parseId(uri);
				selection = selection == null ? "id=" + id : selection + "id=" + id;
			case TABLE_INDEX_DOWNLOAD:
				affectNum = db.update(TABLE_DOWNLOAD_NAME, values, selection,
						selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("illegal uri");
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return affectNum;
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			db.setTransactionSuccessful();
			getContext().getContentResolver().notifyChange(TABLE_DOWNLOAD_URI, null);
			return results;
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private class MySQLiteOpenHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "browsers_db";
		private static final int DB_VERSION = 1;

		public MySQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuffer buffer = new StringBuffer("CREATE TABLE ");
			buffer.append(TABLE_INDEX_NAVI_NAME + "(").append(TABLE_NAVI_ID)
					.append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
					.append(TABLE_NAVI_TITLE).append(" TEXT NOT NULL,")
					.append(TABLE_NAVI_URL).append(" TEXT NOT NULL,")
					.append(TABLE_NAVI_REALURL).append(" TEXT NOT NULL,")
					.append(TABLE_NAVI_POSITION).append(" INTEGER,")
					.append(TABLE_NAVI_ICON).append(" BLOB").append(");");
			db.execSQL(buffer.toString());
			buffer = new StringBuffer("CREATE TABLE ");
			buffer.append(TABLE_FAV_NAME + "(").append(TABLE_FAV_ID)
					.append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
					.append(TABLE_FAV_TITLE).append(" TEXT NOT NULL,")
					.append(TABLE_FAV_URL).append(" TEXT NOT NULL,")
					.append(TABLE_FAV_REALURL).append(" TEXT NOT NULL,")
					.append(TABLE_FAV_ICON).append(" BLOB").append(");");
			db.execSQL(buffer.toString());
			buffer = new StringBuffer("CREATE TABLE ");
			buffer.append(TABLE_HIS_NAME + "(").append(TABLE_HIS_ID)
					.append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
					.append(TABLE_HIS_TITLE).append(" TEXT NOT NULL,")
					.append(TABLE_HIS_URL).append(" TEXT NOT NULL,")
					.append(TABLE_HIS_REALURL).append(" TEXT NOT NULL,")
					.append(TABLE_HIS_ICON).append(" BLOB,")
					.append(TABLE_HIS_ACCESS_TIME).append(" INTEGER")
					.append(");");
			db.execSQL(buffer.toString());
			buffer = new StringBuffer("CREATE TABLE ");
			buffer.append(TABLE_DOWNLOAD_NAME + "(").append(TABLE_DOWNLOAD_ID)
					.append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
					.append(TABLE_DOWNLOAD_FILE_NAME).append(" TEXT NOT NULL,")
					.append(TABLE_DOWNLOAD_MIMETYPE).append(" TEXT NOT NULL,")
					.append(TABLE_DOWNLOAD_URL).append(" TEXT NOT NULL,")
					.append(TABLE_DOWNLOAD_FILE_SIZE).append(" INTEGER,")
					.append(TABLE_DOWNLOAD_CURRENT_SIZE).append(" INTEGER,")
					.append(TABLE_DOWNLOAD_STATUS).append(" INTEGER")
					.append(");");
			db.execSQL(buffer.toString());
		}

		// ALTER TABLE Subscription ADD COLUMN Activation BLOB;
		private static final String ALTER_COLUMN_INTO_TABLE = "ALTER TABLE %1$s ADD COLUMN %2$s";

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// db.execSQL(String.format(ALTER_COLUMN_INTO_TABLE,
			// TABLE_INDEX_NAVI_NAME,
			// TABLE_NAVI_REALURL + "TEXT NOT NULL"));
			// db.execSQL(String.format(ALTER_COLUMN_INTO_TABLE, TABLE_FAV_NAME,
			// TABLE_FAV_REALURL + "TEXT NOT NULL"));
			// db.execSQL(String.format(ALTER_COLUMN_INTO_TABLE, TABLE_HIS_NAME,
			// TABLE_HIS_REALURL + "TEXT NOT NULL"));
			// db.execSQL(String.format(ALTER_COLUMN_INTO_TABLE,
			// TABLE_DOWNLOAD_NAME,
			// TABLE_DOWNLOAD_MIMETYPE + "TEXT NOT NULL"));
		}

	}

}
