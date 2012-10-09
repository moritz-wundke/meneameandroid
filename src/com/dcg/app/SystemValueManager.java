package com.dcg.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;

import com.dcg.provider.SystemValue;

public class SystemValueManager {
	private static final String TAG = "SystemValueManager";

	static {
		ApplicationMNM.addLogCat(TAG);
	}

	/**
	 * Set a specific persistent system value
	 * 
	 * @param contentResolver
	 * @param key
	 * @param value
	 */
	public static void setSystemValue(Context context,
			String key, String value) {
		try {
			// Get the current content resolver
			final ContentResolver resolver = context.getContentResolver();
			final ContentValues values = new ContentValues();
			values.put(SystemValue.KEY, key);
			values.put(SystemValue.VALUE, value);
			SystemValue systemValue = getSystemValue(context, key);

			// If the system value is already there just update it
			if (systemValue == null) {
				// Add new value
				resolver.insert(SystemValue.CONTENT_URI, values);
				ApplicationMNM.logCat(TAG, "[INSERT] SystemValue " + key + "("
						+ value + ") set");
			} else {
				// Use the URI to update the item
				if (resolver
						.update(systemValue.getUri(), values, null, null) > 0) {
					ApplicationMNM.logCat(TAG, "[UPDATE] SystemValue " + key
							+ "(" + value + ")");
				} else {
					ApplicationMNM.logCat(TAG, "[UPDATE] [FAILED] SystemValue "
							+ key + "(" + value + "): " + systemValue.getUri());
				}
			}
		} catch (SQLException e) {
			ApplicationMNM.logCat(TAG, "Failed to set system value " + key
					+ ":" + e.toString());
		}
	}

	/**
	 * Recover a system value tag
	 * 
	 * @param contentResolver
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static SystemValue getSystemValue(
			Context context, String key) {
		String[] projection = new String[] { BaseColumns._ID, SystemValue.VALUE };
		final String[] selectionArgs = new String[1];
		selectionArgs[0] = key;
		final String selection = SystemValue.KEY + "=?";
		
		// Handeled data
		SystemValue result=null;
		Cursor cur=null;
		
		// Get the current content resolver
		final ContentResolver resolver = context.getContentResolver();
		cur = resolver.query(SystemValue.CONTENT_URI, projection,
				selection, selectionArgs, null);
		if (cur != null && cur.moveToFirst()) {
			result = new SystemValue();
			result.setKey(key);
			result.setValue(cur.getString(cur.getColumnIndex(SystemValue.VALUE)));

			// Create the item URI
			result.setUri( ContentUris.withAppendedId(SystemValue.CONTENT_URI,
					cur.getLong(cur.getColumnIndex(BaseColumns._ID))));

			cur.close();
			ApplicationMNM.logCat(TAG, "[QUERY] SystemValue " + key + "("
					+ result.getValue() + ") recovered");
		} else {
			ApplicationMNM.logCat(TAG, "[QUERY] [FAILED] SystemValue " + key
					+ " not found in DB");
		}
		
		if (cur != null)
		{
			cur.close();
		}
		return result;
	}
}
