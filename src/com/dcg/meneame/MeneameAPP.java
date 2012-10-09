package com.dcg.meneame;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

import com.dcg.app.ApplicationMNM;
import com.dcg.app.SystemValueManager;
import com.dcg.dialog.VersionChangesDialog;
import com.dcg.provider.SystemValue;
import com.dcg.util.BuildInterface;
import com.dcg.util.TabHostConfigurator;

/**
 * Main activity, basically holds the main tab widget
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class MeneameAPP extends TabActivity {

	/** Class tag used for it's logs */
	private static final String TAG = "MeneameAPP";

	/** Size of the text indicator of the tab */
	private static final float SIZE_OF_TABTEXT = 15.0f;

	/** Main app TabHost */
	private TabHost mTabHost;

	/** Main animation */
	private Animation mMainAnimation = null;

	/** All listeners used to cache a app restart */
	private List<RestartAppListener> mRestartListeners = new ArrayList<RestartAppListener>();
	
	/** Flag used to store the last active tab */
	public static final String LAST_ACTIVE_TAB = "last.active.tab";

	// Get some global stuff

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("Meneame For Android","Build Version: "+Build.VERSION.RELEASE);

		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");
		ApplicationMNM.registerMainActivity(this);

		this.createContent();

		// Check version number and if we change the version show a nice dialog
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int savedVersion = prefs.getInt("pref_app_version_number", 0);
		ApplicationMNM.logCat(TAG,
				"Current version: " + ApplicationMNM.getVersionNumber() + " ("
						+ ApplicationMNM.getVersionLable() + ")");
		ApplicationMNM.logCat(TAG, "Saved version: " + savedVersion);
		// Did we made any update?
		if (ApplicationMNM.getVersionNumber() > savedVersion) {
			VersionChangesDialog versionDialog = new VersionChangesDialog(this);
			versionDialog.show();

			// Save the version
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("pref_app_version_number",
					ApplicationMNM.getVersionNumber());

			// Don't forget to commit your edits!!!
			editor.commit();
		}
		// Did we got a crash last time?
		else if (!hasExitedSuccessfully()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.crash_report_question)
					.setCancelable(false)
					.setPositiveButton(R.string.generic_yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									sendCrashReport();
								}
							})
					.setNegativeButton(R.string.generic_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	protected void configureTabWidgetAnd1x( TabWidget tabWidget ) {
		// 
	}
	
	protected void configureTabWidgetAnd2x( TabWidget tabWidget ) {
		//
		//tabWidget.setStripEnabled(true);
		//Log.d("AAAA", ">>>>>>>>>>>>>>>> 2x");
	}
	
	public void configureTabHost( TabHost tabHost ) {
		String tabHostConfiguratorClass = "";
		switch(BuildInterface.getAPILevel()) {
		case BuildInterface.API_LEVEL_8:
			tabHostConfiguratorClass = "com.dcg.util.TabHostConfigurator_2x";
			break;
		case BuildInterface.API_LEVEL_9:
			tabHostConfiguratorClass = "com.dcg.util.TabHostConfigurator_2x";
			break;
		}
		
		// If no configuratoir has been found do nothing
		if ( tabHostConfiguratorClass.equals("") ) return;
		
		try {
			TabHostConfigurator configurator = (TabHostConfigurator) Class.forName(tabHostConfiguratorClass).newInstance();
			
			// Configure it
			configurator.configuraeTabHost(tabHost);
		} catch(Exception e) {
			ApplicationMNM.warnCat("TabConfigurator", "Failed to create configurator: " + e.toString());
		}
	}

	private void createContent() {
		if( isTiny() ) {
			setContentView(R.layout.main_tiny);
		} else {
			setContentView(R.layout.main);
		}
		mTabHost = getTabHost();
		
		this.createTabs(mTabHost, SIZE_OF_TABTEXT);
		
		// Enable strips for our layout. We always want this to be true!
		//TabWidget tabWidget = mTabHost.getTabWidget();
		configureTabHost(mTabHost);
		
		try {
			//tabWidget.setStripEnabled(true);
			
			//android:tabStripEnabled="true"
			//android:tabStripLeft="@drawable/tab_bottom_left"
			//android:tabStripRight="@drawable/tab_bottom_right"
		} catch (Exception e) {
			// Nothing to be done
		}

		// Set news tab as visible one
		mTabHost.setCurrentTab(getSystemValue(LAST_ACTIVE_TAB, 0));
	}

	private void createTabs(TabHost mTabHost, float textSize) {
		TabSpec[] tabSpecs = new TabSpec[3];
		String[] texts = new String[3];
		Class<? extends FeedActivity>[] classes = new Class[3];
		ColorStateList colorList = null;
		XmlResourceParser[] parser = new XmlResourceParser[3];
		Drawable[] background = new StateListDrawable[3];

		try {
			parser[0] = getResources().getXml(
					R.color.color_state_definition_tab);
			colorList = ColorStateList.createFromXml(getResources(), parser[0]);
			parser[0] = getResources().getXml(R.drawable.tab_indicator);
			parser[1] = getResources().getXml(R.drawable.tab_indicator);
			parser[2] = getResources().getXml(R.drawable.tab_indicator);
			background[0] = StateListDrawable.createFromXml(getResources(),
					parser[0]);
			background[1] = StateListDrawable.createFromXml(getResources(),
					parser[1]);
			background[2] = StateListDrawable.createFromXml(getResources(),
					parser[2]);
			tabSpecs[0] = mTabHost.newTabSpec(NewsActivity
					.static_getTabActivityTag());
			tabSpecs[1] = mTabHost.newTabSpec(QueueActivity
					.static_getTabActivityTag());
			tabSpecs[2] = mTabHost.newTabSpec(CommentsActivity
					.static_getTabActivityTag());
			texts[0] = getResources().getString(
					NewsActivity.static_getIndicatorStringID());
			texts[1] = getResources().getString(
					QueueActivity.static_getIndicatorStringID());
			texts[2] = getResources().getString(
					CommentsActivity.static_getIndicatorStringID());
			classes[0] = NewsActivity.class;
			classes[1] = QueueActivity.class;
			classes[2] = CommentsActivity.class;

		} catch (XmlPullParserException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < 3; i++) {
			configureTab(inflater, colorList, background[0], textSize,
					tabSpecs[i], classes[i], texts[i]);
			mTabHost.addTab(tabSpecs[i]);
		}
	}

	private void configureTab(LayoutInflater inflater, ColorStateList text,
			Drawable background, float textSize, TabSpec tab,
			Class<? extends FeedActivity> clazz, String indicatorStringId) {
		tab.setContent(new Intent(this, clazz));

		// Create the tab indicator from a layout file
		View TabIndicator = inflater.inflate(R.layout.tab_indicator, null);

		// Add text to the text view and all the over stuff
		TextView textView = (TextView) TabIndicator
				.findViewById(R.id.tab_title);
		textView.setText(indicatorStringId);
		textView.setTextSize(textSize);
		textView.setTextColor(text);
		textView.setGravity(Gravity.CENTER);

		// Now add the indicator view
		this.setIndicator(tab, indicatorStringId, TabIndicator);
	}

	private void setIndicator(TabHost.TabSpec tabSpec, String label, View view) {
		// This is because setIndicator(View v) does not exist in andrid <1.6
		try {
			Method m = tabSpec.getClass().getMethod("setIndicator", View.class);
			m.invoke(tabSpec, view);
		} catch (Exception e) {
			// in case if platform 1.5 or via other problems indicator cannot be
			// set as view
			// we have to set as just simple label.
			tabSpec.setIndicator(label);
		}
	}

	@Override
	protected void onStart() {
		ApplicationMNM.logCat(TAG, "onStart()");
		super.onStart();
	}

	/**
	 * Add a new app restart listener
	 * 
	 * @param listener
	 */
	public void addAppRestartListener(RestartAppListener listener) {
		if (!mRestartListeners.contains(listener)) {
			mRestartListeners.add(listener);
		}
	}

	/**
	 * Remove an existing restart listener
	 * 
	 * @param listener
	 */
	public void removeAppRestartListener(RestartAppListener listener) {
		if (mRestartListeners.contains(listener)) {
			mRestartListeners.remove(listener);
		}
	}

	@Override
	protected void onRestart() {
		ApplicationMNM.logCat(TAG, "onRestart()");
		ApplicationMNM.registerMainActivity(this);
		
		// Launch any restart listeners
		Iterator<RestartAppListener> it = mRestartListeners.iterator();
		while(it.hasNext()) {
			RestartAppListener listener = it.next();
			if ( listener != null ) {
				listener.onAppRestart();
			}
		}
		
		mTabHost.clearAllTabs();
		this.createContent();
		
		super.onRestart();
	}

	@Override
	protected void onPause() {
		ApplicationMNM.logCat(TAG, "onPause()");
		if ( mTabHost != null )
			setSystemValue(LAST_ACTIVE_TAB, mTabHost.getCurrentTab());
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		ApplicationMNM.logCat(TAG, "onDestroy()");
		ApplicationMNM.clearCachedContext();

		// Clear our restart listeners
		mRestartListeners.clear();

		// Before we destroy the app we need to save our system value to
		// say that we closed the app properly!
		setExitSuccessfull();

		// Destroy app
		super.onDestroy();
	}

	/** Marks the exit as ok */
	private void setExitSuccessfull() {
		if (ApplicationMNM.mbAllowCrashReport) {
			// TODO: Finish this part
		}
	}

	/** Looks if we exited successfully the app last time */
	public boolean hasExitedSuccessfully() {
		if (ApplicationMNM.mbAllowCrashReport) {
			boolean bResult = false;

			return bResult;
		}
		return true;
	}

	/** Sends a crash report to us */
	public void sendCrashReport() {
		// TODO: Send the log file to us!
	}

	/**
	 * Refresh the animation we will use for the tab page
	 */
	private void initAnim() {
		mMainAnimation = null;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String mainAnim = prefs.getString("pref_style_mainanimation", "None");
		if (mainAnim.compareTo("Fade-in") == 0) {
			mMainAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		} else if (mainAnim.compareTo("Slide-in") == 0) {
			mMainAnimation = AnimationUtils.loadAnimation(this,
					R.anim.slide_bottom);
		}
	}

	/**
	 * Checks if we are using the tiny theme.
	 * 
	 * @return TRUE if tiny theme should be used, FALSE otherwise.
	 */
	private boolean isTiny() {
		// TODO: Tiny stile only available in API level 4 or above
		if ( BuildInterface.isAPILevelAbove(BuildInterface.API_LEVEL_3) ) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String value = prefs.getString("pref_style_theme", "Default");
			return value.compareTo("Tiny") == 0;
		}
		return false;
	}

	@Override
	protected void onStop() {
		ApplicationMNM.logCat(TAG, "onStop()");
		ApplicationMNM.registerMainActivity(null);

		super.onStop();
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

	/** After the activity get's visible to the user */
	@Override
	protected void onResume() {
		super.onResume();
		ApplicationMNM.logCat(TAG, "onResume()");
		ApplicationMNM.setCachedContext(getBaseContext());

		// Start animation stuff
		initAnim();

		if (mMainAnimation != null) {
			mTabHost.startAnimation(mMainAnimation);
		}
	}

	/**
	 * Listener invoked when the main app gets restarted
	 */
	public static interface RestartAppListener {

		/**
		 * Iinvoked when the main app gets restarted
		 */
		void onAppRestart();
	}
}