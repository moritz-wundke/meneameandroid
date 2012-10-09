package com.dcg.meneame;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.dcg.app.ApplicationMNM;
import com.dcg.provider.FeedItemElement;
import com.dcg.task.RequestFeedTaskParams;

public class DetailedArticleActivity extends FeedActivity {

	/** Log tags */
	private static final String TAG = "DetailedArticleActivity";

	/** Are we paused or not? */
	protected boolean mbIsPaused;

	/** Our cached main list view */
	private ListView mListView = null;
	
	/** Article ID used to get data and comments */
	private int mArticleID = -1;
	
	/** Feed ID where the item comes from */
	private int mFeedID = -1;
	
	/** Parent Feed ID where the main article is stored*/
	private int mParentFeedID = -1;
	
	/** The URL used to point to menéame.net */
	private String mLinkURL = "";
	
	/** Source link */
	private String mSource = "";
	
	/** Title of the article */
	private String mTitle = "";
	
	/** Menu items for the detailed view */
	protected final int MENU_DETAIL_SOURCE = MENU_LAST_ITEM+1;
	protected final int MENU_DETAIL_SHARE = MENU_DETAIL_SOURCE+1;
	
	public DetailedArticleActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, "onCreate()");
		
		// From the extra get the article ID so we can start getting the
		// comments
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
		{
			try
			{
				// Find the article id
				mArticleID = Integer.parseInt(extras.getString(EXTRA_KEY_ARTICLE_ID));
				mParentFeedID = Integer.parseInt(extras.getString(EXTRA_KEY_PARENT_FEEDID));
				
				// Get data from DB
				populateDataFromDB();
				
				// Setup view
				setupViews();
				
				// Refresh 
				conditionRefreshFeed();
			
			} catch( Exception e ) {
				// Ok... the data send to open the activity is not correct!
				ApplicationMNM.warnCat(TAG, "No article/parent Feed ID (or not an integer) specified in extra bundle!");
				finish();
			}
		}
		else
		{
			ApplicationMNM.warnCat(TAG, "No bundle send when opening this activity!");
			finish();
		}
	}
	
	/**
	 * Called when our app logo image button receives a touch event
	 * @param v
	 * @param event
	 * @return
	 */
	protected boolean onAppLogoTouch( View v, MotionEvent event ) {
		finish();
		return true;
	}
	
	/**
	 * The conversation is in presented in inverse order than the list of
	 * articles!
	 * 
	 * @return boolean
	 */
	public boolean shouldStackFromBottom() {
		return !super.shouldStackFromBottom();
	}
	
	@Override
	protected void setupViews() {
		// Only setup the view if we got a valid article ID
		if ( mArticleID > -1 )
		{
			super.setupViews();
		}
	}
	
	@Override
	protected void conditionRefreshFeed() {
		// Only refresh if we got a valid article ID
		if ( mArticleID > -1 )
		{
			super.conditionRefreshFeed();
		}
	}
	
	/**
	 * Set the content view we will use for the activity
	 */
	protected void setupContentView() {
		// Prepare layout
		setContentView(R.layout.detailed_article);
	}
	
	/**
	 * Return the ID used for this feed tab</br></br> <i>NOTE:</i> Negative
	 * values are used for our tab and other know FeedActivities. This is so
	 * because the article detailed view is also a feed of data and comments and
	 * the id is the article ID which is positive
	 */
	@Override
	public int getFeedID() {
		return mArticleID;
	}
	
	/**
	 * By default we will use articels
	 * 
	 * @return
	 */
	@Override
	public int getFeedItemType() {
		return FeedItemElement.TYPE_COMMENT;
	}
	
	/**
	 * Take the article data from the database and save it in the activity
	 */
	public void populateDataFromDB() {
		if ( mArticleID > -1 )
		{
			// Set data
			// Form an array specifying which columns to return.
			String[] projection = new String[] { BaseColumns._ID, 
					FeedItemElement.FEEDID, FeedItemElement.COMMENT_RSS,
					FeedItemElement.TITLE, FeedItemElement.VOTES, FeedItemElement.LINK,
					FeedItemElement.DESCRIPTION, FeedItemElement.CATEGORY,
					FeedItemElement.URL, FeedItemElement.PUB_DATE,
					FeedItemElement.USER };

			final String[] arguments1 = new String[1];
			arguments1[0] = String.valueOf(mArticleID);
			final String where = FeedItemElement.LINK_ID + "=?";

			// Make the query.
			Cursor cur = managedQuery(FeedItemElement.CONTENT_URI, projection,
					where, arguments1, null);

			// Print all articles we got out!
			if (cur != null && cur.moveToFirst()) {
				// Save some internal data
				mFeedID = cur
						.getInt(cur.getColumnIndex(FeedItemElement.FEEDID));
				mFeedURL = cur
						.getString(cur.getColumnIndex(FeedItemElement.COMMENT_RSS));
				mLinkURL = cur
						.getString(cur.getColumnIndex(FeedItemElement.LINK));
				mSource = cur
						.getString(cur.getColumnIndex(FeedItemElement.URL));
				mTitle = cur
						.getString(cur.getColumnIndex(FeedItemElement.TITLE));
				
				// Set view data
				Object viewObj = null;
				viewObj = findViewById(R.id.title);
				if (viewObj != null)
					((TextView) viewObj).setText(mTitle);
				
				viewObj = findViewById(R.id.description);
				if (viewObj != null)
					((TextView) viewObj).setText(cur.getString(cur.getColumnIndex(FeedItemElement.DESCRIPTION)));
				
				viewObj = findViewById(R.id.votes);
				if (viewObj != null)
					((TextView) viewObj).setText(String.valueOf(cur.getInt(cur.getColumnIndex(FeedItemElement.VOTES))));
				
				viewObj = findViewById(R.id.source);
				if (viewObj != null)
					((TextView) viewObj).setText(mSource);
				
				viewObj = findViewById(R.id.tags_content);
				if (viewObj != null)
					((TextView) viewObj).setText(cur.getString(cur.getColumnIndex(FeedItemElement.CATEGORY)));
				
				viewObj = findViewById(R.id.pubDate);
				if (viewObj != null)
					((TextView) viewObj).setText(cur.getString(cur.getColumnIndex(FeedItemElement.PUB_DATE)));
				
				viewObj = findViewById(R.id.user);
				if (viewObj != null)
					((TextView) viewObj).setText(cur.getString(cur.getColumnIndex(FeedItemElement.USER)));
			}

			// Once we are finished close the cursor
			if (cur != null) {
				cur.close();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Do this only in onCreate() and use restore state to save data
		populateDataFromDB();		
	}
	
	/**
	 * Build the param list for our feed request
	 * @return
	 */
	protected RequestFeedTaskParams getTaskParams() {
		RequestFeedTaskParams taskParams = super.getTaskParams();
		taskParams.mParentFeedID = mParentFeedID;
		return taskParams;
	}
	
	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, R.string.main_menu_refresh).setIcon(
				R.drawable.ic_menu_refresh);
		
		// Add options related to the article we have in detailed view
		menu.add(1, MENU_DETAIL_SOURCE, 0, R.string.meneo_item_open_source).setIcon(
				android.R.drawable.ic_menu_directions);
		menu.add(1, MENU_DETAIL_SHARE, 0, R.string.meneo_item_share).setIcon(
				android.R.drawable.ic_menu_share);
		return true;
	}
	
	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean bResult = super.onOptionsItemSelected(item);
		if ( ! bResult )
		{
			switch (item.getItemId()) {
			case MENU_DETAIL_SOURCE:
				// Refresh !
				try {
					ApplicationMNM.showToast(getResources().getString(
							R.string.context_menu_open_source));
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mSource)));
				} catch (Exception e) {
					ApplicationMNM.warnCat(TAG, "Can not open URI in browser: "
							+ e.toString());
				}
				return true;
			case MENU_DETAIL_SHARE:
				// Open notame activity
				shareArticleLink(mSource, mTitle);
				return true;
			}
		}
		return false;
	}
}
