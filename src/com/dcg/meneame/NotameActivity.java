package com.dcg.meneame;

import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.R;
import com.dcg.util.HttpManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Notame activity that handles all actions the notame api can handle
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class NotameActivity extends Activity {
	/** Class tag used for it's logs */
	private static final String TAG = "NotameActivity";

	/** Result filed when the msg has been send to nótame */
	private static final String NOTAME_RESULT_KEY = "notame_result";

	/** Result codes used by this activity */
	private static int NOTAME_RESULT_CODE_BACK = 0;

	/** thread msg handler */
	private Handler mHandler = null;

	/** notame thread! */
	private NotameThread notameThread = null;

	/** Basic nótame data */
	private static final String NOTAME_URL = "http://meneame.net/api/newpost.php";
	private static final String NOTAME_USER_FIELD = "user";
	private static final String NOTAME_API_KEY = "key";
	private static final String NOTAME_MSG = "text";

	/** Views we need to take into account */
	private EditText mEditTextView = null;
	private Button mbtnSend = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");

		setContentView(R.layout.notame);

		// Get edit text
		mEditTextView = (EditText) findViewById(R.id.notame_text);
		mEditTextView.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				mbtnSend
						.setEnabled(mEditTextView.getText().toString().length() > 0);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// Set button callbacks
		mbtnSend = (Button) findViewById(R.id.btnSend);
		mbtnSend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				sendNotameMessage();
			}
		});
		mbtnSend.setEnabled(false);

		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				back();
			}
		});
		
		// Set the imamge button callback
		ImageButton logoButton = (ImageButton)findViewById(R.id.applogo);
		if ( logoButton != null )
		{
			logoButton.setOnTouchListener(new ImageButton.OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					back();
					return true;
				}
			});
		}
	}

	public void sendNotameMessage() {
		if (mEditTextView.getText().toString().compareTo("") != 0) {
			// Disbale message
			mbtnSend.setEnabled(false);
			mbtnSend.setText(R.string.notame_sending);

			// Create a new handler
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					handleThreadMessage(msg);
				}
			};

			// Send
			notameThread = new NotameThread();
			notameThread.start();
		}
	}

	/** Called when the thread tells us */
	private void handleThreadMessage(Message msg) {
		Bundle data = msg.getData();
		if (data == null) {
			ApplicationMNM.showToast(R.string.notame_failed);
		} else {
			boolean bSuccessfull = data.getBoolean(NOTAME_RESULT_KEY, false);
			ApplicationMNM.showToast(bSuccessfull ? R.string.notame_ok
					: R.string.notame_failed);
		}

		mbtnSend.setText(R.string.generic_send);
		mEditTextView.setText("");
		mHandler = null;
		notameThread = null;
	}

	public void back() {
		ApplicationMNM.logCat(TAG, "Back");
		setResult(NOTAME_RESULT_CODE_BACK, null);
		finish();
	}

	/**
	 * Encode a data text into a notame one
	 * 
	 * @param s
	 * @return
	 */
	public static String encodeNotameText(String rawText) {
		if (rawText != null) {
			StringBuffer buffer = new StringBuffer();
			int i = 0;
			try {
				while (true) {
					int c = rawText.charAt(i++);
					if ((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5A)
							|| (c >= 0x61 && c <= 0x7A)) {
						buffer.append((char) c);
					} else {
						buffer.append("%");
						if (c <= 0xf) {
							buffer.append("0");
						}
						buffer.append(Integer.toHexString(c));
					}
				}
			} catch (Exception e) {
			}
			return buffer.toString();
		}
		return null;
	}

	/**
	 * Nota add notes thread
	 * 
	 * @author Moritz Wundke (b.thax.dcg@gmail.com)
	 */
	public class NotameThread extends Thread {

		@Override
		public void run() {
			boolean bResult = false;
			ApplicationMNM.logCat(TAG, "Sending nótame message");

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String userName = prefs.getString("pref_account_user", "");
			String APIKey = prefs.getString("pref_account_apikey", "");
			String text = mEditTextView.getText().toString();

			String URL = NOTAME_URL + "?" + NOTAME_USER_FIELD + "=" + userName
					+ "&" + NOTAME_API_KEY + "=" + APIKey + "&charset=utf-8&"
					+ NOTAME_MSG + "=" + encodeNotameText(text);
			HttpGet httpGet = new HttpGet(URL);
			try {

				// Execute
				HttpResponse response = HttpManager.execute(httpGet);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					InputStreamReader inputStream = new InputStreamReader(
							response.getEntity().getContent());
					int data = inputStream.read();
					String finalData = "";
					while (data != -1) {
						finalData += (char) data;
						data = inputStream.read();
					}
					inputStream.close();
					// Did we got an ok?
					bResult = finalData.startsWith("OK");
				}
			} catch (Exception e) {
				// Nothing to be done
				ApplicationMNM.warnCat(TAG, "Failed to send notame msg! " + e);
			}

			if (mHandler != null) {
				Message msg = mHandler.obtainMessage();
				Bundle data = new Bundle();
				data.putBoolean(NOTAME_RESULT_KEY, bResult);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		}
	}
}
