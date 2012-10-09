package com.dcg.meneame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.dcg.app.ApplicationMNM;
import com.dcg.dialog.VersionChangesDialog;
import com.dcg.provider.FeedItemElement;
import com.dcg.util.BuildInterface;

/**
 * Our preference activity
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class Preferences extends PreferenceActivity {
	/** Class tag used for it's logs */
	private static final String TAG = "Preferences";

	/** Default result code */
	public static final int RESULT_CODE_DEFAULT = 0x0000;

	/**
	 * Returned when we need to force a list view refresh when turning back from
	 * the preference screen
	 */
	public static final int RESULT_CODE_REFRESH_LIST_VIEW = 0x0001;

	/**
	 * Returned when we need to refres our views once we leave the prefernces
	 */
	public static final int RESULT_CODE_SETUP_VIEWS = 0x0002;
	
	/** resuklt code holder */
	private int mResultCode = RESULT_CODE_DEFAULT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");

		// Add prefs from xml
		addPreferencesFromResource(R.xml.preferences);

		// Set version title!
		PreferenceScreen prefScreen = getPreferenceScreen();
		if (prefScreen != null) {
			PreferenceGroup appPrefernce = (PreferenceGroup) prefScreen
					.getPreference(1);
			if (appPrefernce != null) {
				Preference versionPrefernce = appPrefernce.getPreference(0);
				if (versionPrefernce != null) {
					String versionTitle = getResources().getString(
							getResources().getIdentifier("version_title",
									"string", ApplicationMNM.static_getPackageName()));
					versionTitle = versionTitle.replaceAll("NUMBER", String
							.valueOf(ApplicationMNM.getVersionNumber()));
					versionTitle = versionTitle.replaceAll("LABLE",
							ApplicationMNM.getVersionLable());
					versionPrefernce.setTitle(versionTitle);
				}
			}
			
			// Disable theme selection for versions that are not 1.6 or more
			if ( BuildInterface.isAPILevelUnder(BuildInterface.API_LEVEL_4) ) {
				appPrefernce = (PreferenceGroup) prefScreen
					.getPreference(2);
				if (appPrefernce != null) {
					Preference themePrefernce = appPrefernce.getPreference(1);
					if (themePrefernce != null) {
						themePrefernce.setEnabled(false);
						themePrefernce.setSummary(R.string.themes_not_available_sdk_version);
					}
				}
			}
		}
	}

	/**
	 * Update the result code with a new 'or'ed value :D. This way we can add
	 * quite some flags
	 * 
	 * @param result
	 */
	private void updateResult(int result) {
		mResultCode |= result;
		setResult(mResultCode);
	}

	/**
	 * Return storage type used
	 * 
	 * @return
	 */
	public String getStorageType() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		return prefs.getString("pref_app_storage", "SDCard");
	}

	@Override
	public void onContentChanged() {
		ApplicationMNM.logCat(TAG, "onContentChanged()");
		super.onContentChanged();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		ApplicationMNM.logCat(TAG, "onContentChanged()");
		if (preference.getKey().compareTo("pref_app_version_number") == 0) {
			VersionChangesDialog versionDialog = new VersionChangesDialog(this);
			versionDialog.show();
		} else if (preference.getKey().compareTo("pref_app_clearcache") == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.confirm_clear_cache).setCancelable(
					false).setPositiveButton(R.string.generic_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							clearFeedCache();
							dialog.dismiss();
						}
					}).setNegativeButton(R.string.generic_no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog clearCacheDialog = builder.create();
			clearCacheDialog.show();
		} else if (preference.getKey().compareTo("pref_app_stack_from_buttom") == 0) {
			updateResult(RESULT_CODE_REFRESH_LIST_VIEW);
		} else if (preference.getKey().compareTo("pref_style_theme") == 0) {
			updateResult(RESULT_CODE_SETUP_VIEWS);
		}
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	/**
	 * Worker method that clears the current feed cache
	 * 
	 * @return
	 */
	public boolean clearFeedCacheWorker() {
		try {
			getContentResolver().delete(FeedItemElement.CONTENT_URI, "", null);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Clear feed cache, file or DB.
	 */
	public void clearFeedCache() {
		if (clearFeedCacheWorker()) {
			ApplicationMNM.logCat(TAG, "Cache has been cleared!");
			ApplicationMNM.showToast(R.string.clear_cache_successfull);
		} else {
			ApplicationMNM.logCat(TAG, "Nothing to be deleted!");
			ApplicationMNM.showToast(R.string.clear_cache_failed);
		}
	}

}
