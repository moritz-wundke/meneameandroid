package com.dcg.provider;

import com.dcg.app.ApplicationMNM;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class SystemValue implements BaseColumns {

	/** Define content provider connections */
	public static final String ELEMENT_AUTHORITY = "systemvalues";
	public static final Uri CONTENT_URI = Uri
			.parse("content://"+ApplicationMNM.static_getPackageName()+"/" + ELEMENT_AUTHORITY);

	/** DB table name */
	public static final String TABLE = "system";

	/** Table definition */
	public static final String KEY = "key";
	public static final int KEY_FIELD = 0;

	public static final String VALUE = "value";
	public static final int VALUE_FIELD = 1;
	
	/** Internal data */
	private String mKey;
	private String mValue;
	private Uri mUri;

	/** Build the right content values */
	public static ContentValues getContentValue(String key, String value) {
		final ContentValues values = new ContentValues();
		values.put(KEY, key);
		values.put(VALUE, value);
		return values;
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		this.mKey = key;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		this.mValue = value;
	}

	public Uri getUri() {
		return mUri;
	}

	public void setUri(Uri uri) {
		this.mUri = uri;
	}	
}