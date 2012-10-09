package com.dcg.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.dcg.provider.RESTfulMethod;

/**
 * The RESTful manager handles and updates any REST methods we are executing.</br>
 * By now it handles states of already executed methods or methods that are </br>
 * a transaction state.</br>
 * </br>
 * The manager operates in a static context and has no instance.</br>
 * The context will be set by ApplicationMNM so you do not need to worry about it.</br>
 * </br>
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RESTfulManager {
	private static final String TAG = "RESTfulManager";

	static {
		ApplicationMNM.addLogCat(TAG);
	}
	
	/**
	 * Adds/Updates a RESTfulMethod in the database
	 * @param item
	 */
	public static void setRESTMethod(Context context, RESTfulMethod method) {
		try {
			// Get the current content resolver
			final ContentResolver resolver = context.getContentResolver();
			final ContentValues contentValues = method.getContentValues();
			
			// try updating the method is that works
			final int count = resolver.update(RESTfulMethod.CONTENT_URI, contentValues, RESTfulMethod.getSelection(), method.getSelectionArgs());
			
			if ( count == 0 )
			{
				try
				{
					resolver.insert(RESTfulMethod.CONTENT_URI,contentValues);
					ApplicationMNM.logCat(TAG, "Added REST method:\n" + method.toString());
				} catch( SQLException e1) {
					ApplicationMNM.warnCat(TAG, "Failed to insert REST method:\n" + method.toString() + "\nError: " + e1.toString());
				}
			}
			else
			{
				ApplicationMNM.logCat(TAG, "Updated REST method:\n" + method.toString());
			}
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed to set REST method:\n" + method.toString() + "\nError: " + e.toString());
		}
	}
	
	/**
	 * Retrieves the RESTfulMethod that is linked to a request if any
	 * @param request
	 * @return
	 */
	public static RESTfulMethod getRESTMethod(Context context, String request) {
		RESTfulMethod method = null;
		Cursor cur = null;
		try {			
			// Get the current content resolver
			final ContentResolver resolver = context.getContentResolver();
			
			// Build method and set request
			method = new RESTfulMethod();
			method.setRequest(request);

			// Query method
			cur = resolver.query(RESTfulMethod.CONTENT_URI, RESTfulMethod.getProjection(),
					RESTfulMethod.getSelection(), method.getSelectionArgs(), null);
			if (cur != null && cur.moveToFirst()) {
				method.buildFromCursor(cur);
			}
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed to get REST method with request:\n" + request + "\nError: " + e.toString());
		}
		
		if ( cur != null )
		{
			cur.close();
		}
		return method;
	}
}
