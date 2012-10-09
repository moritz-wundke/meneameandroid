package com.dcg.util;

import com.dcg.meneame.R;

import android.widget.TabHost;
import android.widget.TabWidget;

public class TabHostConfigurator_2x extends TabHostConfigurator {

	@Override
	public void configuraeTabHost(TabHost tabHost) {
		TabWidget tabWidget = tabHost.getTabWidget();		
		tabWidget.setStripEnabled(true);
		tabWidget.setLeftStripDrawable(R.drawable.tab_bottom_left);
		tabWidget.setRightStripDrawable(R.drawable.tab_bottom_left);
	}

}
