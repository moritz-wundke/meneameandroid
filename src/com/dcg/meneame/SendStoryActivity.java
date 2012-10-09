package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class SendStoryActivity extends Activity {
	/** Log tags */
	private static final String TAG = "SendStoryActivity";
	
	/** The server script that will make the submit */
	private static final String MENEAME_SUBMITT = "http://meneame.net/submit.php?url=ACTUAL_URL&ei=UTF";
	
	private static String mURL = "";
	public SendStoryActivity() {
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
				// Create the share URL
				mURL = extras.getString(Intent.EXTRA_TEXT);
				String finalUrl = MENEAME_SUBMITT.replaceFirst("ACTUAL_URL", mURL);
				
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
				} catch (Exception e) {
					ApplicationMNM.showToast("The link you would like to share is not valid!");
				}
			} catch( Exception e ) {
				// Ok... the data send to open the activity is not correct!
				ApplicationMNM.warnCat(TAG, "No article ID (or not an integer) specified in extra bundle!");
			}
		}
		else
		{
			ApplicationMNM.warnCat(TAG, "No bundle send when opening this activity!");
		}
		finish();
	}
}
