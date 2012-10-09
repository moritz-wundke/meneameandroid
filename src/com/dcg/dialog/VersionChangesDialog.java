package com.dcg.dialog;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VersionChangesDialog extends Dialog {
	/** Log tag */
	private static final String TAG = "VersionChangesDialog";

	public VersionChangesDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		ApplicationMNM.addLogCat(TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_change_dialog);
		setTitle(R.string.version_change_title);

		// When the user presses the ok button just dismiss the dialog
		Button btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				dismiss();
			}
		});

		LinearLayout mainContent = (LinearLayout) findViewById(R.id.version_change_content);
		for (int i = ApplicationMNM.getVersionNumber(); i > 0; i--) {
			// ////////////////////
			// TITLE
			// ////////////////////
			TextView title = new TextView(getContext());

			// Set layout params
			MarginLayoutParams params = new MarginLayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			title.setLayoutParams(params);

			// More params
			title.setSingleLine(true);
			title.setAutoLinkMask(Linkify.WEB_URLS);
			title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			title.setBackgroundColor(R.color.Color_dark_blue);
			title.setPadding(10, 2, 20, 2);

			// Set text
			String resIDText = "version_change_v" + i + "_title";
			int resID = getContext().getResources().getIdentifier(
					resIDText, "string", ApplicationMNM.static_getPackageName());
			if ( resID == 0 ) {
				ApplicationMNM.warnCat("VersionChangesetError", "No resources found for current version ("+i+")");
			}
			title.setText(resID);

			// Add view
			mainContent.addView(title);

			// ////////////////////
			// BODY
			// ////////////////////
			TextView body = new TextView(this.getContext());

			// Set layout params
			params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 0, 0, 0);
			body.setLayoutParams(params);

			// More params
			body.setSingleLine(false);
			body.setAutoLinkMask(Linkify.WEB_URLS);
			body.setPadding(10, 2, 20, 2);

			// Set text
			resID = getContext().getResources().getIdentifier(
					"version_change_v" + i, "string", ApplicationMNM.static_getPackageName());
			body.setText(resID);

			// Add view
			mainContent.addView(body);
		}
	}

}
