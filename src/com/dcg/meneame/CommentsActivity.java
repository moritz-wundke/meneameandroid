package com.dcg.meneame;

import com.dcg.meneame.R;
import com.dcg.provider.FeedItemElement;

import android.os.Bundle;

/**
 * Comments activity
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class CommentsActivity extends FeedActivity {

	static final int LIST_MENU_OPEN = 0;
	static final int LIST_MENU_VOTE = 1;

	public CommentsActivity() {
		super();

		// Set feed
		mFeedURL = "http://www.meneame.net/comments_rss2.php";
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Returns the class name of the list adapter we should use
	 * 
	 * @return
	 */
	@Override
	public String getListAdapterClassName() {
		return "com.dcg.adapter.CommentsAdapter";
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	@Override
	public String getTabActivityTag() {
		return "comments_tab";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	@Override
	public int getIndicatorStringID() {
		return R.string.main_tab_comments;
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "comments_tab";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return R.string.main_tab_comments;
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
	 * Return the ID used for this feed tab</br></br> <i>NOTE:</i> Negative
	 * values are used for our tab and other know FeedActivities. This is so
	 * because the article detailed view is also a feed of data and comments and
	 * the id is the article ID which is positive
	 */
	@Override
	public int getFeedID() {
		return -3;
	}
}
