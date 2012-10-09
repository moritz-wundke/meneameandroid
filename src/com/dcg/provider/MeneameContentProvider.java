package com.dcg.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.dcg.app.ApplicationMNM;

public class MeneameContentProvider extends ContentProvider {
	private static final String TAG = "FeedItemProvider";

	/** Database name */
	private static final String DATABASE_NAME = "data";

	/** Database version currently used. CURRENT 13 - v8 (0.2.2) */
	private static final int DATABASE_VERSION = 14;

	/** Action id's */
	private static final int ITEMS = 1;
	private static final int ITEM_ID = 2;
	private static final int SYSTEM_VALUE = 3;
	private static final int SYSTEM_VALUE_ID = 4;
	private static final int RESTFUL_VALUE = 5;
	private static final int RESTFUL_VALUE_ID = 6;
	
	private static final String AUTHORITY = ApplicationMNM.static_getPackageName();

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, FeedItemElement.ELEMENT_AUTHORITY, ITEMS);
		URI_MATCHER.addURI(AUTHORITY, FeedItemElement.ELEMENT_AUTHORITY + "/#",
				ITEM_ID);
		URI_MATCHER.addURI(AUTHORITY, SystemValue.ELEMENT_AUTHORITY,
				SYSTEM_VALUE);
		URI_MATCHER.addURI(AUTHORITY, SystemValue.ELEMENT_AUTHORITY + "/#",
				SYSTEM_VALUE_ID);
		URI_MATCHER.addURI(AUTHORITY, RESTfulMethod.ELEMENT_AUTHORITY,
				RESTFUL_VALUE);
		URI_MATCHER.addURI(AUTHORITY, RESTfulMethod.ELEMENT_AUTHORITY + "/#",
				RESTFUL_VALUE_ID);
	}

	private SQLiteOpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = "";
		String segment = "";
		switch (URI_MATCHER.match(uri)) {
		case ITEMS:
			qb.setTables(FeedItemElement.TABLE);
			orderBy = FeedItemElement.DEFAULT_SORT_ORDER;
			break;
		case ITEM_ID:
			qb.setTables(FeedItemElement.TABLE);
			segment = uri.getPathSegments().get(1);
			qb.appendWhere(BaseColumns._ID + "=" + segment);
			orderBy = FeedItemElement.DEFAULT_SORT_ORDER;
			break;
		case SYSTEM_VALUE:
			qb.setTables(SystemValue.TABLE);
			break;
		case SYSTEM_VALUE_ID:
			qb.setTables(SystemValue.TABLE);
			segment = uri.getPathSegments().get(1);
			qb.appendWhere(SystemValue.KEY + "='" + segment+"'");
			break;
		case RESTFUL_VALUE:
			qb.setTables(RESTfulMethod.TABLE);
			break;
		case RESTFUL_VALUE_ID:
			qb.setTables(RESTfulMethod.TABLE);
			segment = uri.getPathSegments().get(1);
			qb.appendWhere(RESTfulMethod.REQUEST + "='" + segment+"'");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		if (!TextUtils.isEmpty(sortOrder)) {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case ITEMS:
			return "vnd.android.cursor.dir/vnd."+ApplicationMNM.static_getPackageName()+".provider.feeditems";
		case ITEM_ID:
			return "vnd.android.cursor.item/vnd."+ApplicationMNM.static_getPackageName()+".provider.feeditems";
		case SYSTEM_VALUE:
			return "vnd.android.cursor.dir/vnd."+ApplicationMNM.static_getPackageName()+".provider.systemvalue";
		case SYSTEM_VALUE_ID:
			return "vnd.android.cursor.item/vnd."+ApplicationMNM.static_getPackageName()+".provider.systemvalue";
		case RESTFUL_VALUE:
			return "vnd.android.cursor.dir/vnd."+ApplicationMNM.static_getPackageName()+".provider.systemvalue";
		case RESTFUL_VALUE_ID:
			return "vnd.android.cursor.item/vnd."+ApplicationMNM.static_getPackageName()+".provider.systemvalue";
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = -1;
		boolean bResult = false;
		Uri insertUri = null;
		switch (URI_MATCHER.match(uri)) {
		case ITEMS:
			rowId = db.insert(FeedItemElement.TABLE, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(
						FeedItemElement.CONTENT_URI, rowId);
				bResult = true;
			}
			break;
		case SYSTEM_VALUE:
			rowId = db.insert(SystemValue.TABLE, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(SystemValue.CONTENT_URI,
						rowId);
				bResult = true;
			}
			break;		
		case RESTFUL_VALUE:
			rowId = db.insert(RESTfulMethod.TABLE, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(RESTfulMethod.CONTENT_URI,
						rowId);
				bResult = true;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// return insert URI
		if (bResult) {
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;
		String segment = "";
		switch (URI_MATCHER.match(uri)) {
		case ITEMS:
			count = db.delete(FeedItemElement.TABLE, selection, selectionArgs);
			break;
		case ITEM_ID:
			count = db.delete(FeedItemElement.TABLE, BaseColumns._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case SYSTEM_VALUE:
			count = db.delete(SystemValue.TABLE, selection, selectionArgs);
			break;
		case SYSTEM_VALUE_ID:
			segment = uri.getPathSegments().get(1);
			count = db.delete(SystemValue.TABLE, BaseColumns._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case RESTFUL_VALUE:
			count = db.delete(RESTfulMethod.TABLE, selection, selectionArgs);
			break;
		case RESTFUL_VALUE_ID:
			segment = uri.getPathSegments().get(1);
			count = db.delete(RESTfulMethod.TABLE, BaseColumns._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;
		String segment = "";
		switch (URI_MATCHER.match(uri)) {
		case ITEMS:
			count = db.update(FeedItemElement.TABLE, values, selection,
					selectionArgs);
			break;
		case ITEM_ID:
			segment = uri.getPathSegments().get(1);
			count = db.update(FeedItemElement.TABLE, values,
					BaseColumns._ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		case SYSTEM_VALUE:
			count = db.update(SystemValue.TABLE, values, selection,
					selectionArgs);
			break;
		case SYSTEM_VALUE_ID:
			segment = uri.getPathSegments().get(1);
			count = db.update(SystemValue.TABLE, values, BaseColumns._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case RESTFUL_VALUE:
			count = db.update(RESTfulMethod.TABLE, values, selection,
					selectionArgs);
			break;
		case RESTFUL_VALUE_ID:
			segment = uri.getPathSegments().get(1);
			count = db.update(RESTfulMethod.TABLE, values, BaseColumns._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create items table
			db.execSQL("CREATE TABLE " + FeedItemElement.TABLE + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY, "
					+ FeedItemElement.LINK_ID + " INTEGER, "
					+ FeedItemElement.FEEDID + " INTEGER, "
					+ FeedItemElement.PARENT_FEEDID + " INTEGER, "
					+ FeedItemElement.TITLE + " TEXT, "
					+ FeedItemElement.DESCRIPTION + " TEXT, "
					+ FeedItemElement.LINK + " TEXT, " + FeedItemElement.URL
					+ " TEXT, " + FeedItemElement.COMMENT_RSS + " TEXT, "
					+ FeedItemElement.VOTES + " INTEGER, "
					+ FeedItemElement.CATEGORY + " TEXT, "
					+ FeedItemElement.TYPE + " INTEGER, "
					+ FeedItemElement.PUB_DATE + " TEXT, "
					+ FeedItemElement.USER + " TEXT);");
			db.execSQL("CREATE INDEX itemIndexLinkID ON "
					+ FeedItemElement.TABLE + " (" + FeedItemElement.LINK_ID
					+ ");");
			db.execSQL("CREATE INDEX itemIndexFeedID ON "
					+ FeedItemElement.TABLE + " (" + FeedItemElement.FEEDID
					+ ");");

			// Create system table
			db.execSQL("CREATE TABLE " + SystemValue.TABLE + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY, "
					+ SystemValue.KEY + " TEXT UNIQUE, " + SystemValue.VALUE
					+ " TEXT);");
			db.execSQL("CREATE INDEX systemIndexKey ON " + SystemValue.TABLE
					+ " (" + SystemValue.KEY + ");");
			
			// Create RESTful table
			db.execSQL("CREATE TABLE " + RESTfulMethod.TABLE + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY, "
					+ RESTfulMethod.NAME + " TEXT, "
					+ RESTfulMethod.REQUEST + " TEXT UNIQUE, "
					+ RESTfulMethod.RESULT + " INTEGER, "
					+ RESTfulMethod.STATUS + " INTEGER, "
					+ RESTfulMethod.METHOD + " INTEGER);");
			db.execSQL("CREATE INDEX restfulIndexKey ON " + RESTfulMethod.TABLE
					+ " (" + RESTfulMethod.REQUEST + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			ApplicationMNM.warnCat(TAG, "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FeedItemElement.TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SystemValue.TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + RESTfulMethod.TABLE);
			onCreate(db);
		}
	}

}
