package org.cnc.msrobot.activity;

import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.InputOutput.TextInput;
import org.cnc.msrobot.InputOutput.VoiceToastOutput;
import org.cnc.msrobot.module.ModuleManager;
import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DialogUtils;
import org.cnc.msrobot.utils.SharePrefs;
import org.cnc.msrobot.utils.SpeechToText;
import org.cnc.msrobot.utils.TextToSpeechUtils;
import org.cnc.msrobot.utils.TextToSpeechUtils.SpeechListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class BaseActivity extends FragmentActivity {
	protected Toast mToastCenter;
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	protected ProgressDialog mProgressDialog;
	protected CustomActionBar mActionbar;
	protected SpeechToText mStt = SpeechToText.getInstance();
	protected TextToSpeechUtils mTts = TextToSpeechUtils.getInstance();
	protected DialogUtils mDialog;
	protected final Handler handler = new Handler();
	protected SpeechListener mSpeakAnimationListener;
	protected Input input;
	protected Output output;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Init action bar
		mActionbar = new CustomActionBar(this);
		getActionBar().setCustomView(mActionbar);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// init dialog
		mDialog = new DialogUtils(this);

		// init text to speech and speech to text
		mTts.checkTTSData(this);
		mStt.checkSTT(this);

		// init Input, Output
		input = new TextInput(this);
		output = new VoiceToastOutput(this);

		// config BugSense
		BugSenseHandler.initAndStartSession(this, "f3f143ec");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Init module management
		ModuleManager.getInstance().init(this, input, output);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTts.stopSpeak();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mStt != null) {
			mStt.stopListening();
		}
		if (mActionbar != null) {
			mActionbar.hideRecAnimation();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mTts.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	/**
	 * set speak animation listener
	 * 
	 * @param listener
	 */
	public void setSpeechListener(SpeechListener listener) {
		mTts.setSpeechListenerForModule(listener);
	}

	public void changeIO(Input input, Output output) {
		this.input = input;
		this.output = output;
		// Init module management
		ModuleManager.getInstance().init(this, input, output);
	}

	public TextToSpeechUtils getTextToSpeech() {
		return mTts;
	}

	public SpeechToText getSpeechToText() {
		return mStt;
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
	 * show center toast
	 * 
	 * @param resId
	 *            message resource id
	 */
	public void showCenterToast(int resId) {
		showCenterToast(getString(resId));
	}

	public void showActionBarProgressBar() {
		if (mActionbar != null) {
			mActionbar.showProgressBar();
		}
	}

	public void hideActionBarProgressBar() {
		if (mActionbar != null) {
			mActionbar.hideProgressBar();
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
		if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
	}

	public CustomActionBar getCusomActionBar() {
		return mActionbar;
	}

}
