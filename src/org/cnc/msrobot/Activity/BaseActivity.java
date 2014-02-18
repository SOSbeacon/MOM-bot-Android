package org.cnc.msrobot.Activity;

import org.cnc.msrobot.Utils.SharePrefs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {
	protected Toast mToastCenter;
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayUseLogoEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(Menu.NONE, android.R.id.icon, Menu.NONE, R.string.common_top).setIcon(R.drawable.img_btn_top)
		// .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			return true;
		case android.R.id.icon:
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	/**
	 * Show a toast in the center of screen. Fix: not show if toast is showed
	 * 
	 * @param message
	 *            message to be shown
	 */
	public void showCenterToast(String message) {
		// check if toast not init
		if (mToastCenter == null) {
			mToastCenter = Toast.makeText(this, message, Toast.LENGTH_LONG);
			mToastCenter.setGravity(Gravity.CENTER, 0, 0);
			mToastCenter.show();
			return;
		}
		// set new message
		mToastCenter.setText(message);
		// Check if toast is NOT shown, show again
		if (!mToastCenter.getView().isShown()) {
			mToastCenter.show();
		}
	}

	/**
	 * Show progress dialog with indeterminate loading icon, message and cancelable
	 * 
	 * @param msgResId
	 *            resource message id
	 */
	public void showProgress() {
		// Show progress dialog if it is null or not showing.
		if (mProgressDialog == null || !mProgressDialog.isShowing()) {
			mProgressDialog = ProgressDialog.show(this, "", "", true /* indeterminate */, true /* cancelable */);
		}
	}

	public void dismissProgress() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}
}
