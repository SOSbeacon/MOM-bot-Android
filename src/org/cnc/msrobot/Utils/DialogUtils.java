package org.cnc.msrobot.utils;

import org.cnc.msrobot.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class DialogUtils {
	private static final String TAG = DialogUtils.class.getSimpleName();

	private Context mContext;
	private AlertDialog alertDialog;

	/** Resize dialog or not. */
	private boolean mIsResize = true;

	public DialogUtils(Context context) {
		this.mContext = context;
	}

	public DialogUtils(Context ctx, boolean isResize) {
		this.mContext = ctx;
		mIsResize = isResize;
	}

	/**
	 * Show confirm dialog with Yes, No button
	 * 
	 * @param messageResourceId
	 *            Dialog title Resource Id
	 * @param listener
	 *            Listener for button event
	 */
	public void showConfirmDialog(int messageResourceId, final OnConfirmClickListener listener) {
		showConfirmDialog(null, mContext.getResources().getString(messageResourceId), listener);
	}

	/**
	 * Show confirm dialog with Yes, No button
	 * 
	 * @param titleResourceId
	 *            Dialog title Resource Id
	 * @param messageResourceId
	 *            Dialog message Resource Id
	 * @param listener
	 *            Listener for button event
	 */
	public void showConfirmDialog(int titleResourceId, int messageResourceId, final OnConfirmClickListener listener) {
		showConfirmDialog(mContext.getResources().getString(titleResourceId),
				mContext.getResources().getString(messageResourceId), listener);
	}

	public void showConfirmDialog(int titleResourceId, int messageResourceId) {
		final AlertDialog dialog;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
		dialogBuilder.setTitle(titleResourceId).setMessage(messageResourceId)
				.setPositiveButton(R.string.common_ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		dialog = dialogBuilder.create();
		dialog.show();
		// If not resize do nothing;
		if (mIsResize) resizeDialog(mContext, dialog);
	}

	/**
	 * Show confirm dialog with Yes, No button
	 * 
	 * @param title
	 *            Dialog title text string
	 * @param message
	 *            Dialog message text string
	 * @param listener
	 *            Listener for button event
	 */
	public void showConfirmDialog(String title, String message, final OnConfirmClickListener listener) {
		final AlertDialog dialog;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
		if (title != null) dialogBuilder.setTitle(title);
		final TextView myView = new TextView(mContext);
		myView.setText(message);
		int padding = mContext.getResources().getDimensionPixelSize(R.dimen.common_margin_layout);
		myView.setPadding(padding, padding, padding, padding);
		dialogBuilder.setView(myView);
		dialogBuilder.setPositiveButton(R.string.common_yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) listener.onConfirmOkClick();
			}
		}).setNegativeButton(R.string.common_no, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (listener != null) listener.onConfirmCancelClick();
			}
		});
		dialog = dialogBuilder.create();
		dialog.show();
		// If not resize do nothing;
		if (mIsResize) resizeDialog(mContext, dialog);
	}

	/**
	 * Show alert dialog with ok button
	 * 
	 * @param messageResourceId
	 *            Message Resource Id
	 * @param listener
	 *            Listener of OK button
	 */
	public void showAlertDialog(int messageResourceId, final OnClickListener listener) {
		showAlertDialog(mContext.getResources().getString(messageResourceId), listener);
	}

	/**
	 * Show alert dialog with ok button
	 * 
	 * @param message
	 *            Message text string
	 * @param listener
	 *            Listener of OK button
	 */
	public void showAlertDialog(CharSequence message, final OnClickListener listener) {
		final AlertDialog dialog;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
		dialogBuilder.setMessage(message).setPositiveButton(R.string.common_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (listener != null) listener.onClick(dialog, which);
			}
		});
		dialog = dialogBuilder.create();
		dialog.show();
		// If not resize do nothing;
		if (mIsResize) resizeDialog(mContext, dialog);
	}

	/**
	 * show selection dialog which list item
	 * 
	 * @param titleResId
	 *            dialog title string resource
	 * @param arrayItemResId
	 *            dialog item array resource
	 * @param listener
	 *            listener for item click
	 */
	public void showSelectionDialog(int titleResId, int arrayItemResId, final DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setItems(arrayItemResId, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClick(dialog, which);
				}
			}
		});
		builder.setTitle(titleResId);
		final AlertDialog dialog = builder.create();
		dialog.show();
		// If not resize do nothing;
		if (mIsResize) resizeDialog(mContext, dialog);
	}

	/**
	 * show selection dialog which list item
	 * 
	 * @param titleResId
	 *            dialog title string resource
	 * @param listItems
	 *            array string item list
	 * @param listener
	 *            listener for item click
	 */
	public void showSelectionDialog(int titleResId, String[] listItems, final DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setItems(listItems, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClick(dialog, which);
				}
			}
		});
		builder.setTitle(titleResId);
		final AlertDialog dialog = builder.create();
		dialog.show();
		// If not resize do nothing;
		if (mIsResize) resizeDialog(mContext, dialog);
	}

	/**
	 * Resize with of dialog smaller
	 * 
	 * @param dialog
	 */
	public static void resizeDialog(Context context, AlertDialog dialog) {
		// Get display width
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int spacing = 200;
		@SuppressWarnings("deprecation")
		int width = display.getWidth();
		// Change width, height
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		// Set width of dialog is 4/5 screen's width
		lp.width = width - spacing;
		dialog.getWindow().setAttributes(lp);
	}

	/**
	 * Resize with of dialog smaller
	 * 
	 * @param dialog
	 */
	public static void resizeDialog(AlertDialog dialog, int width) {
		// Change width, height
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		// Set width of dialog is 4/5 screen's width
		lp.width = width;
		dialog.getWindow().setAttributes(lp);
	}

	public interface OnMenuClickListener {
		/**
		 * Menu click event
		 * 
		 * @param position
		 */
		void onMenuClick(int position, String key);
	}

	public interface OnConfirmClickListener {
		/**
		 * Button OK click event
		 */
		void onConfirmOkClick();

		/**
		 * Button Cancel click event
		 */
		void onConfirmCancelClick();
	}

	/**
	 * Dismiss dialog.
	 */
	public void dismissDialog() {
		if (alertDialog != null) alertDialog.dismiss();
	}
}
