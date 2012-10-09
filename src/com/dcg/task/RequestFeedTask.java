package com.dcg.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.dcg.app.ApplicationMNM;
import com.dcg.app.RESTfulManager;
import com.dcg.meneame.FeedActivity;
import com.dcg.provider.FeedItemElement;
import com.dcg.provider.RESTfulMethod;
import com.dcg.rss.RSSParser;
import com.dcg.rss.RSSParser.AddFeedItemListener;
import com.dcg.util.HttpManager;
import com.dcg.util.UserTask;

/**
 * Will download a feed from the net and cache it to the database
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RequestFeedTask extends
		UserTask<RequestFeedTaskParams, Void, Integer> implements
		AddFeedItemListener {
	private static final String TAG = "RequestFeedTask";
	
	/** Authority used for the task, this should never be used as normal content provider access! */
	public static final String ELEMENT_AUTHORITY = "RequestFeedTask";
	
	/** The observer URI, used to catch any finish event */
	public static final Uri CONTENT_URI = Uri
	.parse("content://"+ApplicationMNM.static_getPackageName()+"/" + ELEMENT_AUTHORITY);

	/** Error keys used as return types by the task */
	public static final int ERROR_COULD_NOT_CREATE_RSS_HANDLER = ApplicationMNM.ERROR_FAILED + 1;
	public static final int ERROR_RSS_UNKOWN = ERROR_COULD_NOT_CREATE_RSS_HANDLER + 1;
	public static final int ERROR_INVALID_RSS_DATA = ERROR_RSS_UNKOWN + 1;
	public static final int ERROR_NO_INPUT_STREAM = ERROR_INVALID_RSS_DATA + 1;
	public static final int ERROR_NO_INPUT_STREAM_EXCEPTION = ERROR_NO_INPUT_STREAM + 1;
	public static final int ERROR_NO_INPUT_STREAM_FILE_NOT_FOUND = ERROR_NO_INPUT_STREAM_EXCEPTION + 1;
	public static final int ERROR_NO_INPUT_STREAM_UNKOWN_HOST = ERROR_NO_INPUT_STREAM_FILE_NOT_FOUND + 1;
	public static final int ERROR_NO_INPUT_STREAM_ILLEGAL_STATE = ERROR_NO_INPUT_STREAM_UNKOWN_HOST + 1;
	public static final int ERROR_CREATE_FEEDITEM_ACCESS = ERROR_NO_INPUT_STREAM_ILLEGAL_STATE + 1;
	public static final int ERROR_CREATE_FEEDITEM_INSTANCE = ERROR_CREATE_FEEDITEM_ACCESS + 1;
	public static final int ERROR_CREATE_FEEDITEM_CLASS_NOT_FOUND = ERROR_CREATE_FEEDITEM_INSTANCE + 1;
	public static final int ERROR_RSS_SAX = ERROR_CREATE_FEEDITEM_CLASS_NOT_FOUND + 1;
	public static final int ERROR_RSS_IO_EXCEPTION = ERROR_RSS_SAX + 1;
	public static final int ERROR_RSS_PARSE_CONFIG = ERROR_RSS_IO_EXCEPTION + 1;
	public static final int ERROR_NOT_A_FEED_ACTIVITY = ERROR_RSS_PARSE_CONFIG + 1;
	public static final int ERROR_NO_PARAMS = ERROR_NOT_A_FEED_ACTIVITY + 1;

	/** Parser types */
	public static final int PARSER_TYPE_DEFAULT = 0;

	/** data params for the task */
	private RequestFeedTaskParams mMyParams;

	/** Feed parser */
	private RSSParser mFeedParser = null;
	
	/** cached Context used to handle content access */
	private Context mContext = null;
	
	/** The method we are using */
	private RESTfulMethod mRESTMethod = null;

	/**
	 * Constructor
	 */
	public RequestFeedTask(Activity activity) {
		super(activity);
		ApplicationMNM.addLogCat(TAG);
	}

	@Override
	public Integer doInBackground(RequestFeedTaskParams... params) {
		Integer bResult = ApplicationMNM.ERROR_SUCCESSFULL;
		
		// Get the base context
		mContext = mActivity.getBaseContext();
		
		// We add the task!		
		mRESTMethod = new RESTfulMethod();

		// We need params to run
		if (params[0] != null) {
			// Get params
			mMyParams = params[0];
			try {
				ApplicationMNM.logCat(TAG, "Requesting feed...");
				ApplicationMNM.logCat(TAG, "  FeedID = "
						+ ((FeedActivity) mActivity).getFeedID());
				ApplicationMNM.logCat(TAG, "  mURL = " + mMyParams.mURL);
				ApplicationMNM.logCat(TAG, "  mItemClass = "
						+ mMyParams.mItemClass);
				ApplicationMNM.logCat(TAG, "  mMaxItems = "
						+ mMyParams.mMaxItems);
				
				// Setup method
				mRESTMethod.setMethod(RESTfulMethod.REST_GET);
				mRESTMethod.setStatus(RESTfulMethod.STATUS_TRANSACTION);
				mRESTMethod.setRequest(mMyParams.mURL);
				mRESTMethod.setName(TAG);
				
				// Update RESTful method
				RESTfulManager.setRESTMethod(mContext,mRESTMethod);
				
				// Get stream from the net
				InputStreamReader streamReader = getInputStreamReader(mMyParams.mURL);
				try {
					// Delete cache before staring the parsing process
					if (((FeedActivity) mActivity).deleteFeedCache())
						ApplicationMNM.logCat(TAG, " Cache deleted");
					else
						ApplicationMNM.logCat(TAG, " Unable to delete cache");

					// Create the parser
					mFeedParser = getRSSParser(PARSER_TYPE_DEFAULT);
					mFeedParser.setInputStream(streamReader);
					mFeedParser.setmFeedItemClassName(mMyParams.mItemClass);
					mFeedParser.setMaxItems(mMyParams.mMaxItems);
					mFeedParser.onAddFeedItemListener(this);
					mFeedParser.parse();

				} catch (IllegalAccessException e) {
					ApplicationMNM.warnCat(TAG, e.toString());
					bResult = ERROR_RSS_UNKOWN;
				} catch (InstantiationException e) {
					ApplicationMNM.warnCat(TAG, e.toString());
					bResult = ERROR_RSS_UNKOWN;
				} catch (ClassNotFoundException e) {
					ApplicationMNM.warnCat(TAG, e.toString());
					bResult = ERROR_RSS_UNKOWN;
				} catch (ClassCastException e) {
					ApplicationMNM.warnCat(TAG, e.toString());
					bResult = ERROR_NOT_A_FEED_ACTIVITY;
				}
			} catch (ClientProtocolException e1) {
				ApplicationMNM.warnCat(TAG, e1.toString());
				bResult = ERROR_RSS_UNKOWN;
			} catch (IOException e1) {
				ApplicationMNM.warnCat(TAG, e1.toString());
				bResult = ERROR_RSS_UNKOWN;
			} catch (URISyntaxException e1) {
				ApplicationMNM.warnCat(TAG, e1.toString());
				bResult = ERROR_RSS_UNKOWN;
			} catch ( Exception e1 ) {
				ApplicationMNM.warnCat(TAG, e1.toString());
				bResult = ERROR_RSS_UNKOWN;
			}
		} else {
			bResult = ERROR_NO_PARAMS;
		}

		// Cleanup!
		mFeedParser = null;

		return bResult;
	}

	/**
	 * Request this thread to stop what it's doing
	 */
	public void requestStop(boolean mayInterruptIfRunning) {
		if (mFeedParser != null)
			mFeedParser.requestStop();
		cancel(mayInterruptIfRunning);
	}

	/**
	 * Will get the input stream reader the feed reader will use to parse it
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected InputStreamReader getInputStreamReader(String streamURL)
			throws ClientProtocolException, IOException, URISyntaxException {
		HttpGet request = new HttpGet();
		
		// Clean the URL for containing invalid characters
		streamURL = streamURL.replaceAll("\t", "");
		streamURL = streamURL.replaceAll("\n", "");
		streamURL = streamURL.replaceAll("\r", "");

		request.setURI(new URI(streamURL));
		ApplicationMNM.logCat(TAG, "Starting request " + request.toString());

		HttpResponse response = HttpManager.execute(request);

		if (response != null) {
			return new InputStreamReader(response.getEntity().getContent());
		}
		return null;
	}

	/**
	 * Create a new feed parser
	 * 
	 * @param parserClass
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private RSSParser getRSSParser(int parserType)
			throws IllegalAccessException, InstantiationException,
			ClassNotFoundException {
		return new RSSParser();
	}

	@Override
	public void onPostExecute(Integer result) {
		//ApplicationMNM.showToast("Finished with result: " + result);
		
		if (mMyParams != null) 
		{
			mRESTMethod.setResult(result);
			mRESTMethod.setStatus((result==ApplicationMNM.ERROR_SUCCESSFULL)?RESTfulMethod.STATUS_DONE:RESTfulMethod.STATUS_FIELD);
			
			// Update RESTful method
			RESTfulManager.setRESTMethod(mContext,mRESTMethod);
			
			ContentResolver resolver = getContentResolver();
			if ( resolver != null )
			{
				// Inform any registered content observers
				Uri onFinishedUri = ContentUris.withAppendedId(CONTENT_URI, result);
				resolver.notifyChange(onFinishedUri, null);
			}
		}
		
		// Null refs
		mActivity = null;
		mContext = null;
		mRESTMethod = null;
	}

	/**
	 * Access the current content provider
	 * 
	 * @return
	 */
	public ContentResolver getContentResolver() {
		if ( mContext != null )
		{
			return mContext.getContentResolver();
		}
		return null;
	}

	/**
	 * Called from the RSS parser when a feed item has been parsed
	 */
	public void onFeedAdded(FeedItemElement feedItem) {
		// Set some feed specific values
		feedItem.setFeedID(((FeedActivity) mActivity).getFeedID());
		feedItem.setParentFeedID(mMyParams.mParentFeedID);
		feedItem.setType(((FeedActivity) mActivity).getFeedItemType());

		// Print it out
		ApplicationMNM.logCat(TAG, "FeedParsed: " + feedItem.getLinkID());

		try {
			// Insert the feed item
			getContentResolver().insert(FeedItemElement.CONTENT_URI,
					feedItem.getContentValues());
		} catch (SQLException e) {
			// Something went wrong!
			ApplicationMNM.warnCat(TAG, String.valueOf(e.getStackTrace()));
		}
	}

	/**
	 * Listener invoked by
	 * {@link com.dcg.task.RequestFeedTask#doInBackground(RequestFeedTaskParams...)}
	 * Once a feed has been finished processing.
	 */
	public static interface RequestFeedListener {

		/**
		 * Invoked when we finished a feed request. If the result code is !0
		 * from 0 an error occurred.
		 * 
		 * @param resultCode
		 * @param feed
		 */
		void onFeedFinished(Integer resultCode);
	}
}
