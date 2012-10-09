package com.dcg.util;

import com.dcg.meneame.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Based on Romain Guys UIUtilities from the shelves app
 * (http://code.google.com/p/shelves/)
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class UIUtilities {
	private UIUtilities() {
	}

	public static void showImageToast(Context context, int id, Drawable drawable) {
		final View view = LayoutInflater.from(context).inflate(
				R.layout.image_toast_notification, null);
		((TextView) view.findViewById(R.id.message)).setText(id);
		((ImageView) view.findViewById(R.id.cover)).setImageDrawable(drawable);

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(view);

		toast.show();
	}

	public static void showToast(Context context, int id) {
		showToast(context, id, false);
	}

	public static void showToast(Context context, int id, boolean longToast) {
		Toast.makeText(context, id,
				longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public static void showFormattedImageToast(Context context, int id,
			Drawable drawable, Object... args) {

		final View view = LayoutInflater.from(context).inflate(
				R.layout.image_toast_notification, null);
		((TextView) view.findViewById(R.id.message)).setText(String.format(
				context.getText(id).toString(), args));
		((ImageView) view.findViewById(R.id.cover)).setImageDrawable(drawable);

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(view);

		toast.show();
	}

	public static void showFormattedToast(Context context, int id,
			Object... args) {
		Toast.makeText(context,
				String.format(context.getText(id).toString(), args),
				Toast.LENGTH_LONG).show();
	}
}
