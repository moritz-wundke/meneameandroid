package com.dcg.provider;

import com.dcg.app.ApplicationMNM;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class RESTfulMethod implements BaseColumns {
	private static final String TAG = "SystemValueManager";
	
	/** Define content provider connections */
	public static final String ELEMENT_AUTHORITY = "RESTfulmethod";
	public static final Uri CONTENT_URI = Uri
			.parse("content://"+ApplicationMNM.static_getPackageName()+"/" + ELEMENT_AUTHORITY);

	/** DB table name */
	public static final String TABLE = "RESTful";
	
	/** Available method types */
	public static final int REST_GET = 0;
	public static final int REST_POST = 1;
	public static final int REST_INSERT = 2;
	public static final int REST_DELETE = 3;
	
	/** Available status types */
	public static final int STATUS_TRANSACTION = 0;
	public static final int STATUS_DONE = 1;
	public static final int STATUS_FAILED = 2;
	
	/** Table definition */
	public static final String NAME = "name";
	public static final int NAME_FIELD = 0;

	public static final String REQUEST = "request";
	public static final int REQUEST_FIELD = 1;
	
	public static final String STATUS = "status";
	public static final int STATUS_FIELD = 2;
	
	public static final String METHOD = "method";
	public static final int METHOD_FIELD = 3;
	
	public static final String RESULT = "result";
	public static final int RESULT_FIELD = 4;
	
	/** Some database stuff */
	private static String[] sArguments1 = new String[1];
	private static final String sSelection;
	private static final String[] sProjection;
	
	/** Internal data */
	private long mID = -1;
	private String mName = "";
	private String mRequest = "";
	private int mStatus = -1;
	private int mMethod = -1;
	private int mResult = -1;
	
	static {
        StringBuilder selection = new StringBuilder();
        selection.append(REQUEST);
        selection.append("=?");
        sSelection = selection.toString();
        
        // Build projection
        sProjection = new String[] {
        		BaseColumns._ID, 
        		RESTfulMethod.NAME,
        		RESTfulMethod.REQUEST,
        		RESTfulMethod.STATUS,
        		RESTfulMethod.METHOD,
        		RESTfulMethod.RESULT
        		};
        
        ApplicationMNM.addLogCat(TAG);
	}
	
	/** Build the right content values */
	public ContentValues getContentValues() {
		final ContentValues values = new ContentValues();

		values.put(NAME, mName);
		values.put(REQUEST, mRequest);
		values.put(STATUS, mStatus);
		values.put(METHOD, mMethod);
		values.put(RESULT, mResult);
		
		return values;
	}
	
	/** Get the selection string used by this object */
	public static String getSelection() {
		return sSelection;
	}
	
	/** Return the selection arguments */
	public String[] getSelectionArgs() {
		final String[] arguments1 = sArguments1;
		arguments1[0] = mRequest;
		return arguments1;
	}
	
	/** Get the selection string used by this object */
	public static String[] getProjection() {
		return sProjection;
	}
	
	/**
	 * Fill the item from values taken from a valid cursor.</br>
	 * NOTE: We assume the cursor is valid for his item!
	 * @param cursor
	 * @return
	 */
	public boolean buildFromCursor( Cursor cursor ) {
		try
		{
			mID = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
			mName = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
			mRequest = cursor.getString(cursor.getColumnIndexOrThrow(REQUEST));
			mStatus = cursor.getInt(cursor.getColumnIndexOrThrow(STATUS));
			mMethod = cursor.getInt(cursor.getColumnIndexOrThrow(METHOD));
			mResult = cursor.getInt(cursor.getColumnIndexOrThrow(RESULT));
		} catch( IllegalArgumentException e ) {
			clear();
			ApplicationMNM.warnCat(TAG, "Failed to build REST method from cursos: " + e.toString());
			return false;
		}
		return true;
	}
	
	/**
	 * Clear all data
	 */
	public void clear() {
		mID = -1;
		mName = "";
		mRequest = "";
		mStatus = -1;
		mMethod = -1;
		mResult = -1;
	}
	
	public void setID(long ID) {
		this.mID = ID;
	}

	public long getID() {
		return mID;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getRequest() {
		return mRequest;
	}

	public void setRequest(String request) {
		this.mRequest = request;
	}

	public int getStatus() {
		return mStatus;
	}

	public void setStatus(int status) {
		this.mStatus = status;
	}

	public int getMethod() {
		return mMethod;
	}

	public void setMethod(int method) {
		this.mMethod = method;
	}
	
	public int getResult() {
		return mResult;
	}

	public void setResult(int result) {
		this.mResult = result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		// Start Object
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);

		// Add data
		result.append(" mName: " + mName + NEW_LINE);
		result.append(" mRequest: " + mRequest + NEW_LINE);
		result.append(" mStatus: " + mStatus + NEW_LINE);
		result.append(" mMethod: " + mMethod + NEW_LINE);
		result.append(" mResult: " + mResult + NEW_LINE);
		
		// End object
		result.append("}");
		return result.toString();
	}
}
