package com.dcg.meneame;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.dcg.adapter.FeedItemAdapter;
import com.dcg.adapter.FeedItemViewHolder;
import com.dcg.app.ApplicationMNM;
import com.dcg.app.RESTfulManager;
import com.dcg.app.SystemValueManager;
import com.dcg.dialog.AboutDialog;
import com.dcg.meneame.MeneameAPP.RestartAppListener;
import com.dcg.provider.FeedItemElement;
import com.dcg.provider.RESTfulMethod;
import com.dcg.provider.SystemValue;
import com.dcg.task.MenealoTask;
import com.dcg.task.RequestFeedTask;
import com.dcg.task.RequestFeedTaskObserver;
import com.dcg.task.RequestFeedTaskParams;
import com.dcg.task.RequestFeedTask.RequestFeedListener;
import com.dcg.util.BuildInterface;

/**
 * Basic activity that handles feed parsing and stuff like that
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
abstract public class FeedActivity extends ListActivity implements
		RequestFeedListener, RestartAppListener {

	/** Log tags */
	private static final String TAG = "FeedActivity";

	/** Our RssWorkerThread class so subclasses will be able to call another one */
	protected static String mRssWorkerThreadClassName = "com.dcg.rss.RSSWorkerThread";
	protected static String mLocalRssWorkerThreadClassName = "com.dcg.rss.LocalRSSWorkerThread";

	/** Feed URL */
	protected String mFeedURL = "";

	/**
	 * Semaphore used by the activities feed worker thread Do we need the
	 * semaphore?
	 */
	// private Semaphore mSemaphore = new Semaphore(1);

	/** Our cached main list view */
	private ListView mListView = null;

	/** Refresh menu item id */
	protected static final int MENU_REFRESH = 0;

	/** Notame menu item id */
	protected static final int MENU_NOTAME = 1;

	/** Settings menu item id */
	protected static final int MENU_SETTINGS = 2;

	/** About menu item id */
	protected static final int MENU_ABOUT = 3;

	/** Last menu items */
	protected static final int MENU_LAST_ITEM = MENU_ABOUT;

	/** Sub activity ID's */
	private static final int SUB_ACT_SETTINGS_ID = 0;
	private static final int SUB_ACT_NOTAME_ID = 1;
	private static final int SUB_ACT_DETAILED_ID = 2;

	/** Context menu options */
	private static final int CONTEXT_MENU_OPEN = 0;
	private static final int CONTEXT_MENU_OPEN_MENEAME = 1;
	private static final int CONTEXT_MENU_OPEN_SOURCE = 2;
	private static final int CONTEXT_MENU_VOTE = 3;
	private static final int CONTEXT_MENU_SHARE = 4;

	/** Definitions used by the detailed view */
	public static final String EXTRA_KEY_ARTICLE_ID = "extra.article.id";
	public static final String EXTRA_KEY_PARENT_FEEDID = "extra.parentfeed.id";
	
	/** Flag used to force a reload of the views on restart */
	public static final String ON_RESTART_SETUP_VIEWS = "on.restart.setup.views";

	/** Flag used to save the last viewed item */
	public static final String LAST_VIEWD_ITEM = "last.viewd.item";
	
	/** Used to debug, will print some data from the DB on start */
	public static final boolean mbPrintDBContentOnStart = false;

	/** Are we paused or not? */
	protected boolean mbIsPaused;

	/** Are we loading a cached feed? */
	protected boolean mbIsLoadingCachedFeed;

	/** Handler used in our ContentObserver */
	private Handler mHandler = null;

	/** Our ContentObservers */
	private RequestFeedTaskObserver mRequestFeedTaskObserver = null;

	/** The logo image button */
	private ImageButton mLogoButton = null;

	public FeedActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onCreate()");

		// Unpause
		mbIsPaused = false;

		// Setup our content view
		setupContentView();

		// Do final stuff
		setupViews();

		// Refresh if needed
		conditionRefreshFeed();
	}

	public void printDatabaseContent() {
		if (mbPrintDBContentOnStart) {
			// Form an array specifying which columns to return.
			String[] projection = new String[] { BaseColumns._ID,
					FeedItemElement.LINK_ID };

			final String[] arguments1 = new String[1];
			arguments1[0] = String.valueOf(this.getFeedID());
			final String where = FeedItemElement.FEEDID + "=?";

			// Make the query.
			Cursor cur = managedQuery(FeedItemElement.CONTENT_URI, projection,
					where, arguments1, null);

			// Print all articles we got out!
			if (cur != null && cur.moveToFirst()) {
				int rowID = 0;
				do {
					ApplicationMNM
							.logCat(TAG,
									"["
											+ rowID
											+ "] FeedItemElement:\n"
											+ " LINK_ID: "
											+ cur.getString(cur
													.getColumnIndex(FeedItemElement.LINK_ID)));
					rowID++;
				} while (cur.moveToNext());
			}

			// Once we are finished close the cursor
			if (cur != null) {
				cur.close();
			}

			// Form an array specifying which columns to return.
			String[] projection2 = new String[] { BaseColumns._ID,
					SystemValue.KEY, SystemValue.VALUE };

			// Make the query.
			cur = managedQuery(SystemValue.CONTENT_URI, projection2, "", null,
					null);

			// Print all articles we got out!
			if (cur != null && cur.moveToFirst()) {
				int rowID = 0;
				do {
					ApplicationMNM
							.logCat(TAG,
									"["
											+ rowID
											+ "] SystemValue:\n"
											+ "   KEY: "
											+ cur.getString(cur
													.getColumnIndex(SystemValue.KEY))
											+ "\n"
											+ " VALUE: "
											+ cur.getString(cur
													.getColumnIndex(SystemValue.VALUE)));
					rowID++;
				} while (cur.moveToNext());
			}

			// Once we are finished close the cursor
			if (cur != null) {
				cur.close();
			}

			// Form an array specifying which columns to return.
			String[] projection3 = new String[] { BaseColumns._ID,
					RESTfulMethod.NAME, RESTfulMethod.REQUEST,
					RESTfulMethod.STATUS, RESTfulMethod.METHOD,
					RESTfulMethod.RESULT };

			// Make the query.
			cur = managedQuery(RESTfulMethod.CONTENT_URI, projection3, "",
					null, null);

			// Print all articles we got out!
			if (cur != null && cur.moveToFirst()) {
				int rowID = 0;
				do {
					ApplicationMNM
							.logCat(TAG,
									"["
											+ rowID
											+ "] RESTfulMethod:\n"
											+ "    NAME: "
											+ cur.getString(cur
													.getColumnIndex(RESTfulMethod.NAME))
											+ "\n"
											+ " REQUEST: "
											+ cur.getString(cur
													.getColumnIndex(RESTfulMethod.REQUEST))
											+ "\n"
											+ "  STATUS: "
											+ cur.getInt(cur
													.getColumnIndex(RESTfulMethod.STATUS))
											+ "\n"
											+ "  METHOD: "
											+ cur.getInt(cur
													.getColumnIndex(RESTfulMethod.METHOD))
											+ "\n"
											+ "  RESULT: "
											+ cur.getInt(cur
													.getColumnIndex(RESTfulMethod.RESULT)));
					rowID++;
				} while (cur.moveToNext());
			}

			// Once we are finished close the cursor
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * Set the content view we will use for the activity
	 */
	protected void setupContentView() {
		// Prepare layout
		setContentView(R.layout.meneo_list);
	}

	@Override
	protected void onStart() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onStart()");
		
		// Add us as a restart listener
		/* @Moss: Not needed right nw so just commented out
		if ( ApplicationMNM.getMainActivity() != null ) {
			ApplicationMNM.getMainActivity().removeAppRestartListener(this);
			ApplicationMNM.getMainActivity().addAppRestartListener(this);
		}
		/**/
		
		super.onStart();
	}

	@Override
	protected void onResume() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onResume()");
		super.onResume();

		// Some debug stuff
		printDatabaseContent();

		// Handler used to catch feed requests
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				// Call the right method
				onFinsihedContentObserver(msg);
			}
		};

		// Register our feed content observer
		mRequestFeedTaskObserver = new RequestFeedTaskObserver(mHandler);
		getContentResolver().registerContentObserver(
				RequestFeedTask.CONTENT_URI, true, mRequestFeedTaskObserver);

		// Set then right state
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		if (isRequestingFeed()) {
			// Still not done
			emptyTextView.setText(R.string.refreshing_lable);

			// Null the adapter!
			mListView.setAdapter(null);
		} else {
			emptyTextView.setText(R.string.empty_list);

			// No adapter? Set one :D
			if (mListView.getAdapter() == null) {
				setupViews();
			}
		}

		// Restore app state if any
		restoreState();

		// Unpause
		mbIsPaused = false;
	}

	@Override
	protected void onRestart() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onRestart()");
		
		// Check if we need to setup the view again
		if ( getSystemValue(this.getTabActivityTag()+"."+ON_RESTART_SETUP_VIEWS, true) ) {
			setupViews();
		}
		
		super.onRestart();
	}

	@Override
	protected void onPause() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onPause()");

		// Save state
		saveState();

		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		emptyTextView.setText("");

		// Unregister our ContentObserver
		getContentResolver()
				.unregisterContentObserver(mRequestFeedTaskObserver);

		// Clean the handler
		mHandler = null;

		// Pause
		mbIsPaused = true;

		// Cleanup
		System.gc();

		super.onPause();
	}

	@Override
	protected void onStop() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onDestroy()");
		// We want to close
		if (isFinishing()) {
			// Delete feed cache if we want to reload it when starting the app
			if (shouldRefreshOnLaunch())
				deleteFeedCache();
		}

		// Finish destroy
		super.onDestroy();
	}

	/**
	 * Performs the selected action that should be done when clicking on an
	 * article
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String value = prefs.getString("pref_app_onitemclick", "Default");
		final FeedItemViewHolder holder = (FeedItemViewHolder) v.getTag();

		if (value.compareTo("Nothing") != 0) {
			// Perform actions
			if (value.compareTo("Detailed") == 0) {
				openDetailedView(holder.link_id);
			} else if (value.compareTo("Meneame") == 0) {
				openBrowser(holder.link);
				ApplicationMNM.showToast(getResources().getString(
						R.string.context_menu_open));
			} else if (value.compareTo("Source") == 0) {
				openBrowser((String) holder.url.getText());
				ApplicationMNM.showToast(getResources().getString(
						R.string.context_menu_open_source));
			} else if (value.compareTo("Vote") == 0) {
				new MenealoTask(this).execute(holder.link_id);
			} else if (value.compareTo("Share") == 0) {
				shareArticleLink((String) holder.url.getText(),
						(String) holder.title.getText());
			}
		}

		super.onListItemClick(l, v, position, id);
	}

	public String getFirstVisiblePositionSystemKey() {
		return "FeedActivity." + getFeedID() + ".FirstVisiblePosKey";
	}
	
	/**
	 * Set a persistent system value
	 * 
	 * @param key
	 * @param value
	 */
	private void setSystemValue(String key,
			int value) {
		setSystemValue( key, String.valueOf(value));
	}
	
	/**
	 * Set a persistent system value
	 * 
	 * @param key
	 * @param value
	 */
	private void setSystemValue(String key,
			boolean value) {
		setSystemValue( key, String.valueOf(value));
	}

	/**
	 * Set a persistent system value
	 * 
	 * @param key
	 * @param value
	 */
	public void setSystemValue(String key, String value) {
		SystemValueManager.setSystemValue(this, key, value);
	}
	
	/**
	 * Get a persistent system value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getSystemValue(String key, int defaultValue) {
		return Integer.parseInt(getSystemValue(key,String.valueOf(defaultValue)));
	}
	
	/**
	 * Get a persistent system value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getSystemValue(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getSystemValue(key,String.valueOf(defaultValue)));
	}

	/**
	 * Get a persistent system value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getSystemValue(String key, String defaultValue) {
		SystemValue value = SystemValueManager.getSystemValue(this, key);
		return (value != null)?value.getValue():defaultValue;
	}

	/**
	 * Save state data into
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ApplicationMNM.logCat(TAG, getTabActivityTag()
				+ "::onSaveInstanceState()");
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		ApplicationMNM.logCat(TAG, getTabActivityTag()
				+ "::onRestoreInstanceState()");
	}
	
	/**
	 * Save the last viewed item in our system db
	 */
	public void setLastViewedItem() {
		if ( mListView != null ) {
			setSystemValue(this.getTabActivityTag()+"."+LAST_VIEWD_ITEM, mListView.getFirstVisiblePosition());
		}
	}
	
	/**
	 * Force an index to be saved as the last viewed item
	 */
	public void setLastViewedItem(int index) {
		if ( mListView != null ) {
			setSystemValue(this.getTabActivityTag()+"."+LAST_VIEWD_ITEM, index);
		}
	}
	
	/**
	 * get the last viewed item index from our db
	 * @return
	 */
	public int getLastViewedItem() {
		// Save state
		if ( mListView != null ) {
			return getSystemValue(this.getTabActivityTag()+"."+LAST_VIEWD_ITEM, 0);
		}
		return 0;
	}

	/**
	 * Save the apps state into the database to be able to recover it again
	 * later
	 */
	private void saveState() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::saveState()");
		try {
			ApplicationMNM.logCat(TAG, " - First visible position: "
					+ mListView.getFirstVisiblePosition());
			setLastViewedItem();
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG,
					"Failed to save app state: " + e.toString());
		}
	}

	/**
	 * Restores a previously saved state into the database and will erase the
	 * cached data after restoring
	 */
	private void restoreState() {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::restoreState()");
		try {
			// Restore state
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG,
					"Failed to restore app state: " + e.toString());
		}
	}

	/**
	 * IF we touch the screen and we do not have any feed and no request has
	 * been made refresh the feed from the net
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ApplicationMNM.logCat(TAG, getTabActivityTag() + "::onTouchEvent()");
		// Only refresh on touch if no feed items there or we are not doing any
		// feed task
		if (!mbIsPaused
				&& (!isRequestingFeed() && mListView != null
						&& mListView.getAdapter() != null && mListView
						.getAdapter().getCount() == 0)) {
			refreshFeed(false);
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Delete an entire feed cache used by us
	 */
	public boolean deleteFeedCache() {
		// Get rid of the last item too
		setLastViewedItem(0);
		
		final String[] arguments2 = new String[2];
		arguments2[0] = arguments2[1] = String.valueOf(getFeedID());
		final String where = FeedItemElement.FEEDID + "=? OR "
				+ FeedItemElement.PARENT_FEEDID + "=?";
		int count = getContentResolver().delete(FeedItemElement.CONTENT_URI,
				where, arguments2);
		return count > 0;
	}

	/**
	 * Should we refresh in launch or not?
	 * 
	 * @return
	 */
	public boolean shouldRefreshOnLaunch() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		return prefs.getBoolean("pref_app_refreshonlaunch", false);
	}

	/**
	 * Refresh from an existing feed or should we start a new request?
	 */
	protected void conditionRefreshFeed() {
		if (shouldRefreshOnLaunch()
				&& (!isRequestingFeed() && mListView != null
						&& mListView.getAdapter() != null && mListView
						.getAdapter().getCount() == 0)) {
			refreshFeed(false);
		}
	}

	/**
	 * Set a cursor adapter for our list
	 */
	protected void setCursorAdapter() {
		String value = "Default";
		if ( BuildInterface.isAPILevelAbove(BuildInterface.API_LEVEL_3) ) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this.getBaseContext());
			value = prefs.getString("pref_style_theme", "Default");
		}
		boolean tiny = value.compareTo("Tiny") == 0;

		// TODO: Use: setFilterText(queryString); to set the filter.
		mListView.setAdapter(new FeedItemAdapter(this, FeedItemElement.FEEDID
				+ "=?", new String[] { String.valueOf(getFeedID()) },
				getFeedItemType(), this.shouldStackFromBottom(), tiny));
	}

	/**
	 * Setup view
	 */
	protected void setupViews() {
		mListView = getListView();

		if (mListView != null) {
			// Stack from bottom or from top?
			mListView.setStackFromBottom(shouldStackFromBottom());

			// Set adapter
			setCursorAdapter();

			// Set basic ListView stuff
			mListView.setTextFilterEnabled(false);

			// Add context menu
			mListView
					.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu,
								View view, ContextMenu.ContextMenuInfo menuInfo) {
							onCreateContextMenuFeedList(menu, view, menuInfo);
						}
					});
			
			// Set the last viewed item index
			mListView.setSelection(getSystemValue(this.getTabActivityTag()+"."+LAST_VIEWD_ITEM,0));
		} else {
			ApplicationMNM.warnCat(TAG, "No ListView found in layout for "
					+ this.toString());
		}

		// Set the imamge button callback
		mLogoButton = (ImageButton) findViewById(R.id.applogo);
		if (mLogoButton != null) {
			mLogoButton.setOnTouchListener(new ImageButton.OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					return onAppLogoTouch(v, event);
				}
			});
		}
	}

	/**
	 * Create the context menu for our feed list
	 * 
	 * @param menu
	 * @param view
	 * @param menuInfo
	 */
	protected void onCreateContextMenuFeedList(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (getFeedItemType() == FeedItemElement.TYPE_ARTICLE) {
			// Open detailed view
			menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.meneo_item_open);

			// Open meneame
			menu.add(0, CONTEXT_MENU_OPEN_MENEAME, 0,
					R.string.meneo_item_open_meneame);
		}

		// Open source or comment in meneame
		menu.add(0, CONTEXT_MENU_OPEN_SOURCE, 0,
				R.string.meneo_item_open_source);

		if (getFeedItemType() == FeedItemElement.TYPE_ARTICLE) {
			// Vote it
			menu.add(0, CONTEXT_MENU_VOTE, 0, R.string.meneo_item_vote);
		}
		// Share action
		menu.add(0, CONTEXT_MENU_SHARE, 0, R.string.meneo_item_share);
	}

	/**
	 * Called when our app logo image button receives a touch event
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	protected boolean onAppLogoTouch(View v, MotionEvent event) {
		// By default we do nothing
		return true;
	}

	/**
	 * Returns the URL this feed points too
	 * 
	 * @return String - FeedURL
	 */
	public String getFeedURL() {
		return mFeedURL;
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	public String getTabActivityTag() {
		return "";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	public int getIndicatorStringID() {
		return -1;
	}

	/**
	 * Return the ID used for this feed tab</br></br> <i>NOTE:</i> Negative
	 * values are used for our tab and other know FeedActivities. This is so
	 * because the article detailed view is also a feed of data and comments and
	 * the id is the article ID which is positive
	 */
	public int getFeedID() {
		return 0;
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return -1;
	}

	/**
	 * By default we will use articels
	 * 
	 * @return
	 */
	public int getFeedItemType() {
		return FeedItemElement.TYPE_ARTICLE;
	}

	/**
	 * Checks if a feed request is currently executed
	 * 
	 * @return true if if a request is active, otherwise false
	 */
	public boolean isRequestingFeed() {
		RESTfulMethod method = RESTfulManager.getRESTMethod(this,
				this.getFeedURL());
		return method != null
				&& method.getStatus() == RESTfulMethod.STATUS_TRANSACTION;
	}

	/**
	 * Build the param list for our feed request
	 * 
	 * @return
	 */
	protected RequestFeedTaskParams getTaskParams() {
		RequestFeedTaskParams taskParams = new RequestFeedTaskParams();
		taskParams.mMaxItems = getMaxItems();
		taskParams.mItemClass = "com.dcg.rss.ArticleFeedItem";
		taskParams.mURL = mFeedURL;
		taskParams.mParserClass = "com.dcg.rss.FeedParser";
		taskParams.mItemType = getFeedItemType();
		return taskParams;
	}

	/**
	 * Will refresh the current feed
	 */
	public void refreshFeed(boolean bUseCache) {
		// Start thread if not started or not alive
		// If we are loading a cached feed to we are pause we can not start!
		if (!mbIsPaused && !isRequestingFeed()) {
			mbIsLoadingCachedFeed = bUseCache;
			
			// Get rid of the last viewed item
			setLastViewedItem(0);

			// Create task and run it
			new RequestFeedTask(this).execute(getTaskParams());

			// Clear the current list adapter!
			setListAdapter(null);

			// Change empty text so that the user knows when it's all done
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.refreshing_lable);
		} else {
			// Mhh already a feed active :P
		}
	}

	/**
	 * Called once we finished requesting the feed
	 */
	public void onFeedFinished(Integer resultCode) {
		if (resultCode == ApplicationMNM.ERROR_SUCCESSFULL) {
			// Set the cursor adapter
			setCursorAdapter();
			// Set empty list text
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
		}
	}

	/**
	 * Called form the ContentResolver to notify us that we finsihed requesting
	 * a feed
	 * 
	 * @param msg
	 */
	public void onFinsihedContentObserver(Message msg) {
		// We always pass a success, the CursorAdapter will set the right list
		// text and items
		onFeedFinished(ApplicationMNM.ERROR_SUCCESSFULL);
	}
	
	/**
	 * Iinvoked when the main app gets restarted
	 */
	public void onAppRestart() {
		// Nothing to be done right now
	}

	/**
	 * Returns the class name of the list adapter we should use
	 * 
	 * @return
	 */
	public String getListAdapterClassName() {
		return "com.dcg.adapter.ArticlesAdapter";
	}

	/**
	 * Return storage type used
	 * 
	 * @return String
	 */
	public String getStorageType() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		return prefs.getString("pref_app_storage", "SDCard");
	}

	/**
	 * Return the maximum items the wants to parse from a feed
	 * 
	 * @return int
	 */
	public int getMaxItems() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		return Integer.parseInt(prefs.getString("pref_app_maxarticles", "-1"));
	}

	/**
	 * Should the list stack from bottom?
	 * 
	 * @return boolean
	 */
	public boolean shouldStackFromBottom() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		return prefs.getBoolean("pref_app_stack_from_buttom", false);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, R.string.main_menu_refresh).setIcon(
				R.drawable.ic_menu_refresh);
		menu.add(1, MENU_NOTAME, 0, R.string.main_menu_notame).setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(1, MENU_SETTINGS, 0, R.string.main_menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(1, MENU_ABOUT, 0, R.string.main_menu_about).setIcon(
				android.R.drawable.ic_menu_info_details);
		return true;
	}

	/** */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(0, !isRequestingFeed());
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			// Refresh !
			refreshFeed(false);
			return true;
		case MENU_NOTAME:
			// Open notame activity
			openNotameScreen();
			return true;
		case MENU_SETTINGS:
			// Open settitngs screen
			openSettingsScreen();
			return true;
		case MENU_ABOUT:
			AboutDialog aboutDialog = new AboutDialog(this);
			aboutDialog.show();
			return true;
		}
		return false;
	}

	/**
	 * Open the detailed view of an article
	 * 
	 * @param articleID
	 *            integer id of the article
	 */
	protected void openDetailedView(int articleID) {
		Intent i = new Intent(this, DetailedArticleActivity.class);
		i.putExtra(EXTRA_KEY_ARTICLE_ID, String.valueOf(articleID));
		i.putExtra(EXTRA_KEY_PARENT_FEEDID, String.valueOf(getFeedID()));
		startActivityForResult(i, SUB_ACT_DETAILED_ID);
	}

	protected void openBrowser(String url) {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG,
					"Can not open URI in browser: " + e.toString());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final FeedItemViewHolder holder = (FeedItemViewHolder) info.targetView
				.getTag();
		switch (item.getItemId()) {
		case CONTEXT_MENU_OPEN:
			// Open detailed view for article
			openDetailedView(holder.link_id);
			break;
		case CONTEXT_MENU_OPEN_MENEAME:
			openBrowser(holder.link);
			ApplicationMNM.showToast(getResources().getString(
					R.string.context_menu_open));
			break;
		case CONTEXT_MENU_OPEN_SOURCE:
			openBrowser((String) holder.url.getText());
			ApplicationMNM.showToast(getResources().getString(
					R.string.context_menu_open_source));
			return true;
		case CONTEXT_MENU_VOTE:
			new MenealoTask(this).execute(holder.link_id);
			return true;
		case CONTEXT_MENU_SHARE:
			// Get link
			shareArticleLink((String) holder.url.getText(),
					(String) holder.title.getText());
			break;
		}
		return false;
	}

	/**
	 * Starts the share process for a specific articel
	 */
	public void shareArticleLink(String url, String title) {
		// send intent
		Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
		sendMailIntent.putExtra(Intent.EXTRA_SUBJECT,
				getResources().getString(R.string.share_option_subject)
						.replace("SUBJECT", title));
		sendMailIntent.putExtra(Intent.EXTRA_TEXT, url);
		sendMailIntent.setType("text/plain");

		startActivity(Intent.createChooser(sendMailIntent, getResources()
				.getString(R.string.share_option_title)));
	}

	/**
	 * Open settings screen
	 */
	public void openSettingsScreen() {
		Intent settingsActivity = new Intent(this, Preferences.class);
		startActivityForResult(settingsActivity, SUB_ACT_SETTINGS_ID);
	}

	/**
	 * get the activity result code
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		boolean bRefreshView =  false;
		switch (requestCode) {
		case SUB_ACT_SETTINGS_ID:
			if ((resultCode & Preferences.RESULT_CODE_REFRESH_LIST_VIEW) == Preferences.RESULT_CODE_REFRESH_LIST_VIEW) {
				bRefreshView = true;
			}
			if ((resultCode & Preferences.RESULT_CODE_SETUP_VIEWS) == Preferences.RESULT_CODE_SETUP_VIEWS) {
				// Set flag so that ur tabs will reload it's views on the next restart call
				if (this.getTabActivityTag().equals(NewsActivity.static_getIndicatorStringID()) ) {
					setSystemValue(NewsActivity.static_getIndicatorStringID()+"."+ON_RESTART_SETUP_VIEWS, true);
				}
				else if (this.getTabActivityTag().equals(QueueActivity.static_getIndicatorStringID()) ) {
					setSystemValue(QueueActivity.static_getIndicatorStringID()+"."+ON_RESTART_SETUP_VIEWS, true);
				}
				else if (this.getTabActivityTag().equals(CommentsActivity.static_getIndicatorStringID()) ) {
					setSystemValue(CommentsActivity.static_getIndicatorStringID()+"."+ON_RESTART_SETUP_VIEWS, true);
				}				
				bRefreshView = true;
			}
			break;
		}
		
		// Refresh view
		if (bRefreshView) setupViews();
	}
	
	/**
	 * Open notame activity
	 */
	public void openNotameScreen() {
		if (hasNotameDataSetup()) {
			Intent notameActivity = new Intent(this, NotameActivity.class);
			startActivityForResult(notameActivity, SUB_ACT_NOTAME_ID);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.notame_setup_data)
					.setCancelable(false)
					.setTitle(R.string.notame_setup_data_tilte)
					.setPositiveButton(R.string.generic_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									openSettingsScreen();
									dialog.dismiss();
								}
							})
					.setNegativeButton(R.string.generic_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog openSettingsDialog = builder.create();
			openSettingsDialog.show();
		}
	}

	/**
	 * Did the user set the needed notame data or not?
	 * 
	 * @return
	 */
	public boolean hasNotameDataSetup() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String userName = prefs.getString("pref_account_user", "");
		String APIKey = prefs.getString("pref_account_apikey", "");
		return userName.compareTo("") != 0 && APIKey.compareTo("") != 0;
	}
}
