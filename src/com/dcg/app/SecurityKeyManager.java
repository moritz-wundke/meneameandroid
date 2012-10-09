package com.dcg.app;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;

import com.dcg.provider.SystemValue;
import com.dcg.util.HttpManager;
import com.dcg.util.IOUtilities;

public class SecurityKeyManager {
	private static final String TAG = "SecurityKeyManager";

	/** URL to request the security key */
	private static final String SECURITY_KEY_REST = "http://m.meneame.net/";

	/** Refresh interval for our key in secons */
	public static final int SECURITY_KEY_REFRESH_INTERVAL = 30;

	/** Security key system values */
	private static final String SECURITY_KEY_TIME = "mmm.securitykey.time";
	private static final String SECURITY_KEY_KEY = "mmm.securitykey.key";

	/** Some constants used to parse our security key */
	private static final String KEY_PREFIX = "var base_key=\"";
	private static final String KEY_SUFFIX = "\";";

	static {
		ApplicationMNM.addLogCat(TAG);
	}

	public static String GetSecurityKey(Context context) {
		boolean bRequestKey = true;
		long now = System.currentTimeMillis();
		String securityKey = "";

		// Check if we need to reload the security key or not
		SystemValue systemValue = SystemValueManager.getSystemValue(context, SECURITY_KEY_TIME);
		if (systemValue.getValue() != null) {
			try {
				// Did elapse enough time to force a new key request?
				if ((now - Long.parseLong(systemValue.getValue())) < SECURITY_KEY_REFRESH_INTERVAL * 1000) {
					bRequestKey = false;
				}
			} catch (Exception e) {
				// No well formatted date so just request the key
			}
		}

		// Request key
		if (bRequestKey) {
			// Final URL
			// NOTE: &u is empty because we do not have any document referrer!
			String URL = SECURITY_KEY_REST;
			HttpEntity entity = null;

			try {
				HttpGet httpGet = new HttpGet(URL);

				// Execute
				HttpResponse response = HttpManager.execute(httpGet);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					entity = response.getEntity();
					InputStream in = null;
					OutputStream out = null;
					try {
						in = entity.getContent();
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream,
								IOUtilities.IO_BUFFER_SIZE);
						IOUtilities.copy(in, out);
						out.flush();
						final String data = dataStream.toString();

						String[] arrayData = data.split("\n");

						// We start at the top because the base_key is at the
						// end of the document
						for (int i = arrayData.length - 1; i >= 0; i--) {
							if (arrayData[i].startsWith(KEY_PREFIX)) {
								// We found the base_key line! So parse it!
								securityKey = arrayData[i].substring(KEY_PREFIX
										.length(), arrayData[i].length()
										- KEY_SUFFIX.length());
								break;
							}
						}

						// Check data
						ApplicationMNM.logCat(TAG, "Security key aquired: "
								+ securityKey);
					} catch (IOException e) {
						ApplicationMNM.warnCat(TAG,
								"Could not request security key " + URL + ": "
										+ e);
					} catch (Exception e) {
						ApplicationMNM.warnCat(TAG,
								"Could not request security key " + URL + ": "
										+ e);
					} finally {
						IOUtilities.closeStream(in);
						IOUtilities.closeStream(out);
					}
				} else {
					ApplicationMNM.warnCat(TAG,
							"Could not request security key " + URL + " code: "
									+ response.getStatusLine().getStatusCode());
				}
			} catch (Exception e) {
				ApplicationMNM.warnCat(TAG, "Could not request security key "
						+ URL + ": " + e);
			} finally {
				if (entity != null) {
					try {
						entity.consumeContent();
					} catch (IOException e) {
						ApplicationMNM.warnCat(TAG, "Could not send vote "
								+ URL + ": " + e);
					}
				}
			}
		}

		// Get the security get from our database
		if (!bRequestKey) {
			systemValue = SystemValueManager.getSystemValue(context, SECURITY_KEY_KEY);
			securityKey = (systemValue != null && systemValue.getValue() != null) ? systemValue.getValue()
					: securityKey;
		}

		// Save requested key and timestamp
		if (bRequestKey) {
			// Save 'now' as security time-stamp
			SystemValueManager.setSystemValue(context, SECURITY_KEY_TIME, String.valueOf(now));
			SystemValueManager.setSystemValue(context, SECURITY_KEY_KEY, securityKey);
		}

		return securityKey;
	}
}
