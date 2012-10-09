package com.dcg.meneame;

import com.dcg.meneame.R;

import android.os.Bundle;

/**
 * News activity
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class NewsActivity extends FeedActivity {

	public NewsActivity() {
		super();

		// Set feed
		mFeedURL = "http://www.meneame.net/rss2.php?local";
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Return the ID used for this feed tab</br></br> <i>NOTE:</i> Negative
	 * values are used for our tab and other know FeedActivities. This is so
	 * because the article detailed view is also a feed of data and comments and
	 * the id is the article ID which is positive
	 */
	@Override
	public int getFeedID() {
		return -1;
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	@Override
	public String getTabActivityTag() {
		return "last_news_tab";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	@Override
	public int getIndicatorStringID() {
		return R.string.main_tab_news;
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * 
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "last_news_tab";
	}

	/**
	 * String id used for the tab indicator
	 * 
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return R.string.main_tab_news;
	}
}
