package com.dcg.task;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RequestFeedTaskObserver extends ContentObserver {
	private static final String TAG = "RequestFeedTaskObserver";
	
	private Handler mHandler = null;

	public RequestFeedTaskObserver(Handler handler) {
		super(handler);
		mHandler = handler;
	}
	
	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		
		
		Message msg = new Message();
		mHandler.sendMessage(msg);
		
		Log.w(TAG, "Finished reqeuesting feed!");
	}

}
