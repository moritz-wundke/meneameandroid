package com.dcg.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dcg.meneame.MeneameAPP;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * General app object used by android life cycle
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ApplicationMNM extends Application {

	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** Emulator IMEI */
	public static final String EMULATOR_IMEI = "000000000000000";

	/** Toast message handler */
	private static Toast mToast = null;

	/** Category used to filter the category list */
	private static List<String> mLogCatList = new ArrayList<String>();

	/** Ignore category list */
	private static List<String> mIgnoreCatList = new ArrayList<String>();

	/** Enable logging or not */
	public static final boolean mbEnableLogging = true;

	/**
	 * Will add the position of the each article in the list, useful for
	 * debugging
	 */
	public static final boolean mbShowArticlePositions = false;

	/** Should we use the crash report functionality? */
	public static final boolean mbAllowCrashReport = false;

	/** Flag used to specify that this is the donation app! */
	public static final boolean mbDonationApp = false;

	/** Cached context to be able to achieve static access */
	private static Context mAppContext = null;

	/** IDs used to handle diffrenet messages comming from different threads */
	public static final String MSG_ID_KEY = "msg.id.key";
	public static final int MSG_ID_ARTICLE_PARSER = 0;
	public static final int MSG_ID_MENEALO = 1;
	public static final int MSG_ID_DB_PARSER = 2;

	/** Definitions of a completed message */
	public static final String COMPLETED_KEY = "msg.completed.key";
	public static final int COMPLETED_OK = 0;
	public static final int COMPLETED_FAILED = 1;

	/** base error for thread messages */
	public static final String ERROR_KEY = "error";
	public static final int ERROR_SUCCESSFULL = 0;
	public static final int ERROR_FAILED = ERROR_SUCCESSFULL + 1;

	/** Feed ID's used by our know FeedActivity's */
	public static final int FEED_ID_NEWS_TAB = -1;
	public static final int FEED_ID_QUEUE_TAB = -2;
	public static final int FEED_ID_COMMENTS_TAB = -3;

	/** Some global definitions */
	public static final String MENEAME_BASE_URL = "http://www.meneame.net";
	
	/** Reference to the main activity which holds the tabs */
	private static MeneameAPP mMeneameApp = null;
	
	/** The REST method manager, this is a singleton */

	@Override
	public void onCreate() {
		super.onCreate();
		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");

		mAppContext = getBaseContext();

		// Create log ignore list!
		// Note: To use a log just comment it :D
		addIgnoreCat(""); // Yep... empty too ;P

		addIgnoreCat("RSSParser");
		addIgnoreCat("RSSWorkerThread");
		addIgnoreCat("FeedItem");
		addIgnoreCat("Feed");
		addIgnoreCat("BaseRSSWorkerThread");
		addIgnoreCat("FeedParser");

		addIgnoreCat("ArticlesAdapter");
		addIgnoreCat("CommentsAdapter");
		addIgnoreCat("Preferences");
		addIgnoreCat("NotameActivity");
		addIgnoreCat("ArticleFeedItem");
		addIgnoreCat("MeneameDbAdapter");
		addIgnoreCat("VersionChangesDialog");
		addIgnoreCat("ArticleDBCacheThread");
		addIgnoreCat("MenealoTask");
		addIgnoreCat("FeedItemAdapter");
		addIgnoreCat("FeedItemStore");
		addIgnoreCat("MenealoTask");

		// Revised class tags
		addIgnoreCat("MeneameAPP");
		addIgnoreCat("ApplicationMNM");
		addIgnoreCat("RequestFeedTask");
		addIgnoreCat("FeedActivity");
		addIgnoreCat("SecurityKeyManager");
		addIgnoreCat("ClientFormLogin");
		addIgnoreCat("RESTfulManager");
		addIgnoreCat("SystemValueManager");
	}
	
	/**
	 * Register the main activity
	 */
	public static void registerMainActivity( MeneameAPP activity ) {
		mMeneameApp = activity;
	}
	
	/**
	 * Return the main activity
	 */
	public static MeneameAPP getMainActivity() {
		return mMeneameApp;
	}

	/**
	 * Clear the cached context, called from the main activity in it's
	 * onDestroy() call.
	 */
	public static void clearCachedContext() {
		setCachedContext(null);
	}

	/**
	 * Set cached context for the app
	 * 
	 * @param context
	 */
	public static void setCachedContext(Context context) {
		mAppContext = context;
	}

	/**
	 * return the current context
	 * 
	 * @return
	 */
	public static Context getCachedContext() {
		return mAppContext;
	}
	
	/**
	 * Returns if we are running in the emulator or not
	 */
	public static boolean isEmulator() {
		if ( mAppContext != null )
		{
			TelephonyManager telephonyManager = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);        
			return !telephonyManager.getDeviceId().equals(EMULATOR_IMEI);
		}
		// No context == not in emulatro?? Mhhh not sure...
		return false;
	}
	
	/**
	 * returns a string representing our package name
	 * @return
	 */
	public static String static_getPackageName() {
		if (mAppContext != null) {
			return mAppContext.getPackageName();
		}
		return "com.dcg.meneame";
	}

	/**
	 * Returns the version number we are currently in
	 */
	public static int getVersionNumber() {
		if (mAppContext != null) {
			try {
				return mAppContext.getPackageManager().getPackageInfo(
						static_getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Returns the current version label
	 * 
	 * @return
	 */
	public static String getVersionLable() {
		if (mAppContext != null) {
			try {
				return mAppContext.getPackageManager().getPackageInfo(
						static_getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * Add a new category to the category ignore list
	 * 
	 * @param cat
	 */
	public static void addIgnoreCat(String cat) {
		if (mbEnableLogging && !mIgnoreCatList.contains(cat)) {
			mIgnoreCatList.add(cat);
		}
	}

	/**
	 * Add a new category to the category log
	 * 
	 * @param cat
	 */
	public static void addLogCat(String cat) {
		if (mbEnableLogging && !mIgnoreCatList.contains(cat)
				&& !mLogCatList.contains(cat)) {
			mLogCatList.add(cat);
		}
	}

	/**
	 * Remove a category from the log
	 * 
	 * @param cat
	 */
	public static void removeLogCat(String cat) {
		if (mbEnableLogging && mLogCatList.contains(cat)) {
			mLogCatList.remove(cat);
		}
	}

	/**
	 * Print a log with a specific category
	 * 
	 * @param msg
	 * @param cat
	 */
	public static void logCat(String cat, String msg) {
		if (mbEnableLogging && mLogCatList.contains(cat)) {
			Log.d(cat, msg);
		}
	}

	/**
	 * Print a warn with a specific category
	 * 
	 * @param msg
	 * @param cat
	 */
	public static void warnCat(String cat, String msg) {
		Log.w(cat, msg);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ApplicationMNM.logCat(TAG, "onLowMemory()");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ApplicationMNM.logCat(TAG, "onLowMemory()");
	}

	/**
	 * Shows a toast message, will hide any already shown message
	 * 
	 * @param msg
	 */
	public static void showToast(String msg) {
		if (mAppContext != null) {
			if (mToast == null) {
				mToast = Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT);
			} else {
				mToast.cancel();
				mToast.setText(msg);
			}
			mToast.show();
		}
	}

	/**
	 * Shows a toast message but referencing a resource id and not directly a
	 * string
	 * 
	 * @param id
	 */
	public static void showToast(int id) {
		showToast(mAppContext.getResources().getString(id));
	}

	/**
	 * Returns the root folder we will use in the SDCard
	 * 
	 * @return
	 */
	public static String getRootSDcardFolder() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "Android" + File.separator + "data"
				+ File.separator + static_getPackageName() + File.separator;
	}

	/**
	 * Returns the root path to our cache foldet
	 * 
	 * @return
	 */
	public static String getRootCacheFolder() {
		return getRootSDcardFolder() + "cache" + File.separator;
	}

	/**
	 * Clear all cached feeds
	 * 
	 * @return
	 */
	public static boolean clearFeedCache() {
		try {
			File directory = new File(getRootCacheFolder());
			fileDelete(directory);
			return true;
		} catch (IOException e) {
			warnCat(TAG, "Failed to clear cache: " + e.toString());
		}
		return false;
	}

	/**
	 * Delete a file/directory recursively
	 * 
	 * @param srcFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void fileDelete(File srcFile) throws FileNotFoundException,
			IOException {
		if (srcFile.isDirectory()) {
			File[] b = srcFile.listFiles();
			for (int i = 0; i < b.length; i++) {
				fileDelete(b[i]);
			}
			srcFile.delete();
		} else {
			srcFile.delete();
		}
	}
}
