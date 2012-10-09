package com.dcg.dialog;

import com.dcg.meneame.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_dialog);
		setTitle(R.string.about_title);

		// When the user presses the ok button just dismiss the dialog
		Button btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				dismiss();
			}
		});
	}
}
