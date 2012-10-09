package com.dcg.util;

import android.os.Build;
import android.widget.TabHost;

/**
 * Class used to handle SDK methods and constants that could be different from one sdk to another
 */
public class BuildInterface {
	
	// API level constants
	public static final int API_LEVEL_INVALID = 0;
	public static final int API_LEVEL_1 = 1;
	public static final int API_LEVEL_2 = 2;
	public static final int API_LEVEL_3 = 3;
	public static final int API_LEVEL_4 = 4;
	public static final int API_LEVEL_5 = 5;
	public static final int API_LEVEL_6 = 6;
	public static final int API_LEVEL_7 = 7;
	public static final int API_LEVEL_8 = 8;
	public static final int API_LEVEL_9 = 9;
	
	/**
	 * Get API Level
	 */
	public static int getAPILevel() {
		// 1.x API's
		if ( Build.VERSION.RELEASE.startsWith("1.6") ) {
			return API_LEVEL_4;
		} else if ( Build.VERSION.RELEASE.startsWith("1.5") ) {
			return API_LEVEL_3;
		} else if ( Build.VERSION.RELEASE.startsWith("1.1") ) {
			return API_LEVEL_2;
		} else if ( Build.VERSION.RELEASE.startsWith("1.0") ) {
			return API_LEVEL_1;
		}
		
		// 2.x API's
		if ( Build.VERSION.RELEASE.startsWith("2.3") ) {
			return API_LEVEL_9;
		} else if ( Build.VERSION.RELEASE.startsWith("2.2") ) {
			return API_LEVEL_8;
		} else if ( Build.VERSION.RELEASE.startsWith("2.1") ) {
			return API_LEVEL_7;
		} else if ( Build.VERSION.RELEASE.startsWith("2.0.1") ) {
			return API_LEVEL_6;
		} else if ( Build.VERSION.RELEASE.startsWith("2.0") ) {
			return API_LEVEL_5;
		}
		
		// Invalid
		return API_LEVEL_INVALID;
	}
	
	/**
	 * Returns TRUE if the provided API Level matches the current systems one
	 */
	public static boolean isAPILevel( int API_LEVEL ) {
		return getAPILevel() == API_LEVEL;
	}	

	public static boolean isAPILevelUnder( int API_LEVEL ) {
		return getAPILevel() < API_LEVEL;
	}
	
	public static boolean isAPILevelAbove( int API_LEVEL ) {
		return getAPILevel() > API_LEVEL;
	}
}
