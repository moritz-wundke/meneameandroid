package com.dcg.task;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.FeedActivity;
import com.dcg.meneame.R;
import com.dcg.util.HttpManager;
import com.dcg.util.IOUtilities;
import com.dcg.util.UserTask;

/**
 * User task that will perform a vote on an article
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class MenealoTask extends UserTask<Integer, Void, Integer> {
	private static final String TAG = "MenealoTask";

	private static final int RESULT_OK = 0;
	private static final int RESULT_FAILED = 1;
	private static final int RESULT_FAILED_USER_CONFIG = 2;
	private static final int RESULT_FAILED_ALREADY_VOTED = 3;
	private static final int RESULT_FAILED_NO_DATA_CONNECTION = 4;

	/** The following constants will define all basic URL's meneame will handle */
	private static final String MENEAME_MENEALO_API = "http://www.meneame.net/backend/menealo.php";

	/**
	 * Constructor
	 */
	public MenealoTask(Activity activity) {
		super(activity);
	}

	@Override
	public Integer doInBackground(Integer... params) {
		int result = RESULT_OK;
		if (hasMenealoDataSetup()) {
			// Request the security key
			// String securityKey =
			// SecurityKeyManager.GetSecurityKey(mActivity);
			/*
			 * try { List<NameValuePair> requestValues = new
			 * ArrayList<NameValuePair>(); requestValues.add(new
			 * BasicNameValuePair("username", "MyUserName"));
			 * requestValues.add(new BasicNameValuePair("pass", "MyPass"));
			 * requestValues.add(new BasicNameValuePair("userip", "MyIP"));
			 * requestValues.add(new BasicNameValuePair("remember",
			 * "PersistenLogin")); requestValues.add(new
			 * BasicNameValuePair("processlogin", "1"));
			 * ClientFormLogin.login("http://www.meneame.net/login.php",
			 * "http://www.meneame.net", requestValues); } catch (Exception e1)
			 * { // TODO Auto-generated catch block e1.printStackTrace(); } /*
			 */

			String securityKey = "";

			// We can start sending the vote
			Integer articleID = params[0];

			// get the user data
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mActivity.getBaseContext());
			int userID = Integer.parseInt(prefs.getString(
					"pref_account_user_id", ""));

			// Final URL
			// NOTE: &u is empty because we do not have any document referrer!
			String URL = MENEAME_MENEALO_API + "?id=" + articleID + "&user="
					+ userID + "&key=" + securityKey + "&u=";
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

						// Check data
						ApplicationMNM.warnCat(TAG, "Vote sended: " + data);
					} catch (IOException e) {
						ApplicationMNM.warnCat(TAG, "Could not send vote "
								+ URL + ": " + e);
						result = RESULT_FAILED;
					} finally {
						IOUtilities.closeStream(in);
						IOUtilities.closeStream(out);
					}
				} else {
					ApplicationMNM.warnCat(TAG, "Could not send vote " + URL
							+ " code: "
							+ response.getStatusLine().getStatusCode());
					result = RESULT_FAILED;
				}
			} catch (Exception e) {
				ApplicationMNM.warnCat(TAG, "Could not send vote " + URL + ": "
						+ e);
				result = RESULT_FAILED_NO_DATA_CONNECTION;
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
		} else {
			result = RESULT_FAILED_USER_CONFIG;
		}
		return result;
	}

	public boolean hasMenealoDataSetup() {
		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mActivity.getBaseContext());
			String APIKey = prefs.getString("pref_account_apikey", "");
			Integer.parseInt(prefs.getString("pref_account_user_id", ""));
			return APIKey.compareTo("") != 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onPostExecute(Integer result) {
		switch (result) {
		case RESULT_OK:
			ApplicationMNM.showToast(R.string.menealo_send);
			break;
		case RESULT_FAILED_USER_CONFIG:
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage(R.string.menealo_setup_data)
					.setCancelable(false).setTitle(
							R.string.menealo_setup_data_tilte)
					.setPositiveButton(R.string.generic_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									((FeedActivity) mActivity)
											.openSettingsScreen();
									dialog.dismiss();
								}
							}).setNegativeButton(R.string.generic_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog openSettingsDialog = builder.create();
			openSettingsDialog.show();
			break;
		case RESULT_FAILED:
			ApplicationMNM.showToast(R.string.menealo_failed);
			break;
		case RESULT_FAILED_ALREADY_VOTED:
			ApplicationMNM.showToast(R.string.menealo_already_voted);
			break;
		case RESULT_FAILED_NO_DATA_CONNECTION:
			ApplicationMNM.showToast(R.string.no_data_connection);
			break;
		}
	}
}