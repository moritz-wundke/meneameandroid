package com.dcg.rss;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dcg.app.ApplicationMNM;
import com.dcg.provider.FeedItemElement;

/**
 * Class that does the parsing of an RSS file
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RSSParser extends DefaultHandler {

	/** log tag for this class */
	private static final String TAG = "RSSParser";

	/** The stream from where to get our data */
	private InputStreamReader mInputStreamReader = null;

	/** Current feed item */
	private FeedItemElement mFeedItem = null;

	/** class name used to create a new feed item */
	private String mFeedItemClassName;

	/** Text we are parsing right now */
	private StringBuilder mText = new StringBuilder();

	/** number of items we want to parse as max */
	private int mMaxItems;

	/** Count items */
	private int mItemCount;

	/** If true we are parsing channel data and we have not reached any item */
	private boolean mbParsingChannel;

	/** Current tag we are in */
	private String mCurrentTag;

	/** did we received a stop request? */
	private boolean mbStopRequested;

	/** listener used to add feed item */
	private AddFeedItemListener mAddFeedItemListener;

	/**
	 * Create a RSSParser passing along a RSS RawData
	 * 
	 * @param RawFeed
	 */
	public RSSParser() {
		this.mText = new StringBuilder();
		this.mItemCount = 0;

		// Add our tag to the category log (so it will be printed out)
		ApplicationMNM.addLogCat(TAG);
	}

	/**
	 * Request this thread to stop what it's doing
	 */
	public void requestStop() {
		mbStopRequested = true;
	}

	public void onAddFeedItemListener(AddFeedItemListener listener) {
		mAddFeedItemListener = listener;
	}

	public String getmFeedItemClassName() {
		return mFeedItemClassName;
	}

	public void setmFeedItemClassName(String mFeedItemClassName) {
		this.mFeedItemClassName = mFeedItemClassName;
	}

	/**
	 * Set the RSS InputStream
	 */
	public void setInputStream(InputStreamReader inputStreamReader) {
		this.mInputStreamReader = inputStreamReader;
	}

	/**
	 * Set max items we will parse
	 * 
	 * @param mMaxItems
	 */
	public void setMaxItems(int mMaxItems) {
		this.mMaxItems = mMaxItems;
	}

	/**
	 * Creates the feed item to be used by the parser
	 */
	private void createFeedItem() {
		// Create the element or clear it's data out
		if (this.mFeedItem == null) {
			this.mFeedItem = new FeedItemElement();
		} else {
			this.mFeedItem.clear();
		}
	}

	/**
	 * Sets new data to the current feed item
	 * 
	 * @param key
	 * @param value
	 */
	private boolean setItemValue(String key, String value) {
		boolean bResult = false;
		if (this.mFeedItem != null) {
			if (key.compareTo("title") == 0) {
				this.mFeedItem.setTitle(value);
				bResult = true;
			} else if (key.compareTo("description") == 0) {
				this.mFeedItem.setDescription(value);
				bResult = true;
			} else if (key.compareTo("votes") == 0) {
				this.mFeedItem.setVotes(Integer.parseInt(value));
				bResult = true;
			} else if (key.compareTo("url") == 0) {
				this.mFeedItem.setURL(value);
				bResult = true;
			} else if (key.compareTo("category") == 0) {
				this.mFeedItem.addCategory(value);
				bResult = true;
			} else if (key.compareTo("link") == 0) {
				this.mFeedItem.setLink(value);
				bResult = true;
			} else if (key.compareTo("commentRss") == 0) {
				this.mFeedItem.setCommentRSS(value);
				bResult = true;
			} else if (key.compareTo("link_id") == 0) {
				this.mFeedItem.setLinkID(Integer.parseInt(value));
				bResult = true;
			} else if (key.compareTo("pubDate") == 0) {
				this.mFeedItem.setPubDate(value);
				bResult = true;
			} else if (key.compareTo("user") == 0) {
				this.mFeedItem.setUser(value);
				bResult = true;
			}
		}
		return bResult;
	}

	/**
	 * This must be called after we parse and got the needed data, so we avoid
	 * any memory leak we could get
	 */
	public void clearReferences() {
		this.mInputStreamReader = null;
		this.mFeedItem = null;
		this.mText = null;
		this.mAddFeedItemListener = null;
	}

	/**
	 * Start RSS parsing
	 */
	public void parse() {
		SAXParserFactory spf = null;
		SAXParser sp = null;

		try {

			spf = SAXParserFactory.newInstance();
			if (spf != null) {
				// Create the fist feed item we will use
				createFeedItem();

				sp = spf.newSAXParser();
				sp.parse(new InputSource(this.mInputStreamReader), this);
			}

			// This is all right, so get rid of the current feed item
			this.mFeedItem = null;
		} catch (RSSParserMaxElementsException e) {
			// Not a real 'error' heheh
			ApplicationMNM.logCat(TAG, "Finished: " + e.toString());
		} catch (RSSParserStopRequestException e) {
			// Not a real 'error' heheh
			ApplicationMNM.logCat(TAG, "Finished: " + e.toString());
		} catch (SAXException e) {
			ApplicationMNM.warnCat(TAG, "Failed parsing: " + e.toString());
		} catch (IOException e) {
			ApplicationMNM.warnCat(TAG, "Failed parsing: " + e.toString());
		} catch (ParserConfigurationException e) {
			ApplicationMNM.warnCat(TAG, "Failed parsing: " + e.toString());
		} catch (Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed parsing: " + e.toString());
		} finally {
			// We should add the last article to the feed (if it exists)
			_addArticle();

			// Clear any references
			clearReferences();
		}
	}

	/**
	 * Called while we read the input stream and parse all items
	 */
	@Override
	public void characters(char[] ch, int start, int length) {
		this.mText.append(ch, start, length);
	}

	/**
	 * Adds the current article to the feed and clears the reference
	 * 
	 * @return
	 */
	private void _addArticle() {
		if (this.mFeedItem != null && mAddFeedItemListener != null) {
			mAddFeedItemListener.onFeedAdded(this.mFeedItem);
		}
	}

	/**
	 * [XML-PARSING] We found a start element
	 */
	@Override
	public void startElement(String uri, String name, String qName,
			Attributes atts) throws RSSParserMaxElementsException,
			RSSParserStopRequestException {
		if (name.length() > 0) {
			// ApplicationMNM.LogCat(TAG, "[START] localName: " +
			// name.toString());

			// Set some global states
			if (name.trim().equals("channel")) {
				ApplicationMNM.logCat(TAG, "Getting channel data...");
				this.mbParsingChannel = true;
			} else if (name.trim().equals("item")) {
				this.mItemCount++;
				ApplicationMNM.logCat(TAG, "Item Found [" + this.mItemCount
						+ "]");
				this.mbParsingChannel = false;

				// Check if we reached the max articles permitted
				// NOTE: The last article will be added by the parser 'always!'
				if (this.mMaxItems > 0 && this.mItemCount > this.mMaxItems) {
					throw new RSSParserMaxElementsException(
							"MAX ELEMENTS REACHED: " + mItemCount, null);
				}

				// Did we got a stop request?
				if (mbStopRequested) {
					throw new RSSParserStopRequestException(
							"Received stop request from parent thread!", null);
				}

				// Add previus article in case we got a previus one
				if (this.mItemCount > 1) {
					_addArticle();
				}

				// Create new article to be hold
				createFeedItem();
			}

			// Register current tag
			mCurrentTag = name;
		}

		// Reset string builder
		this.mText.setLength(0);
	}

	/**
	 * [XML-PARSING] We found an end element. Will fillup the item with data.
	 */
	@Override
	public void endElement(String uri, String name, String qName) {
		if (name.length() > 0) {
			if (!this.mbParsingChannel) {
				if (setItemValue(mCurrentTag.trim(), mText.toString())) {
					ApplicationMNM.logCat(TAG, " - " + mCurrentTag + ": "
							+ mText.toString());
				}
			}
		}
	}

	/**
	 * Listener invoked by {@link com.dcg.rss.RSSParser#_addArticle} Once we
	 * finished creating a feed item
	 */
	public static interface AddFeedItemListener {

		/**
		 * Invoked when we finished to parse a single feed item
		 */
		void onFeedAdded(FeedItemElement feedItem);
	}
}
