package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity implements OnInitListener {
	private static final int CODE_CHECK_TTS = 1;
	private final int NUM_RESULT = 1;
	private final int CODE_LISTEN = 2;
	private final float SPEECH_RATE = 0.5f;
	protected Toast mToastCenter;
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	protected ProgressDialog mProgressDialog;
	protected TextToSpeech mTts;
	protected boolean mRecognizeEnabled = false;
	private final Object setTextToSpeechLock = new Object();
	private boolean syncVariable = false;
	private RecognizeVoiceListener mRecoginzeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayUseLogoEnabled(false);
		}
		initVoiceRecognizor();
		checkTTS();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTts != null) {
			mTts.shutdown();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_CHECK_TTS) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				synchronized (setTextToSpeechLock) {
					syncVariable = true;
					mTts = new TextToSpeech(this, this);
					setTextToSpeechLock.notifyAll();
				}
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == CODE_LISTEN && resultCode == RESULT_OK) {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (mRecoginzeListener != null) mRecoginzeListener.onRecognize(results);
		}
	}

	public void setOnRecogizeVoiceListener(RecognizeVoiceListener listener) {
		mRecoginzeListener = listener;
	}

	public TextToSpeech getTextToSpeech() {
		synchronized (setTextToSpeechLock) {
			while (!syncVariable) {
				try {
					setTextToSpeechLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return mTts;
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// init TTS success, set language and speech rate
			if (mTts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) mTts.setLanguage(Locale.US);
			mTts.setSpeechRate(SPEECH_RATE);
		} else {
			showCenterToast("TTS engine not present");
		}
	}

	/**
	 * prepare to listener for voice regconize
	 */
	public void listen() {
		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, NUM_RESULT);
		startActivityForResult(listenIntent, CODE_LISTEN);
	}

	/**
	 * check for TTS exists
	 */
	private void checkTTS() {
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CODE_CHECK_TTS);
	}

	/**
	 * init voice recognize
	 */
	private void initVoiceRecognizor() {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		if (!list.isEmpty()) {
			mRecognizeEnabled = true;
		} else {
			mRecognizeEnabled = false;
			showCenterToast("Recognizer not present");
		}
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
	 * show center toast
	 * 
	 * @param resId
	 *            message resource id
	 */
	public void showCenterToast(int resId) {
		showCenterToast(getString(resId));
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

	public interface RecognizeVoiceListener {
		void onRecognize(ArrayList<String> data);
	}
}
