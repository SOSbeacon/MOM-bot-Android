package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DialogUtils;
import org.cnc.msrobot.utils.Logger;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

@SuppressWarnings("deprecation")
public class BaseActivity extends FragmentActivity implements OnInitListener, OnUtteranceCompletedListener {
	/**
	 * regconize index
	 */
	protected int mModuleId = 0;
	private static final int DELAY_AFTER_START_BEEP = 100;
	public static final int MAX_RETRY_RECOGNIZE = 3;

	// Stop listening the user input after this period of milliseconds.
	// The input sentences are short and using the app should be snappy so
	// we don't want to spend too much on a single utterance.
	public static final int LISTENING_TIMEOUT = 4000;
	private final float SPEECH_RATE = 1f;
	protected Toast mToastCenter;
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	protected ProgressDialog mProgressDialog;
	protected TextToSpeech mTts;
	private final Object setTextToSpeechLock = new Object();
	private boolean syncVariable = false;
	public int recognizeRetry = 0;
	protected boolean isRecording = false;
	private FragmentRecognizeVoiceListener mRecognizeVoiceListener;
	private SpeechRecognizer mRecognize;
	protected CustomActionBar mActionbar;
	final Handler handler = new Handler();
	protected SpeakAnimationListener mSpeakAnimationListener;
	protected DialogUtils mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionbar = new CustomActionBar(this);
		mDialog = new DialogUtils(this);
		getActionBar().setCustomView(mActionbar);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		checkTTS();
		BugSenseHandler.initAndStartSession(this, "f3f143ec");
	}

	@Override
	protected void onStart() {
		super.onStart();
		initVoiceRecognizor();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTts != null) {
			mTts.shutdown();
		}
	}

	// @Override
	// protected void onStop() {
	// super.onStop();
	// stopSpeak();
	// if (mRecognize != null) {
	// mRecognize.stopListening();
	// }
	// if (mActionbar != null) {
	// mActionbar.hideRecAnimation();
	// }
	// }

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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.REQUEST_CODE_CHECK_TTS) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				synchronized (setTextToSpeechLock) {
					syncVariable = true;
					mTts = new TextToSpeech(this, this);
					mSharePrefs.saveCheckTTS(true);
					setTextToSpeechLock.notifyAll();
				}
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	/**
	 * set speak animation listener
	 * 
	 * @param listener
	 */
	public void setOnSpeakAnimationListener(SpeakAnimationListener listener) {
		mSpeakAnimationListener = listener;
	}

	/**
	 * recognize voice
	 * 
	 * @param data
	 *            array list string
	 */
	public void onRecognize(final ArrayList<String> data) {
	}

	public void setOnRecogizeVoiceListener(FragmentRecognizeVoiceListener listener) {
		mRecognizeVoiceListener = listener;
	}

	protected TextToSpeech getTextToSpeech() {
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
			mTts.setOnUtteranceCompletedListener(this);
		} else {
			showCenterToast("TTS engine not present");
		}
	}

	// It's callback
	public void onUtteranceCompleted(String utteranceId) {
		if (mSpeakAnimationListener != null) {
			mSpeakAnimationListener.stopSpeakingAnimation();
		}
	}

	private final RecognitionListener mRecListener = new RecognitionListener() {

		@Override
		public void onBeginningOfSpeech() {
			Log.d("MsRobot", "onBeginningOfSpeech");
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
		}

		@Override
		public void onEndOfSpeech() {
			Log.d("MsRobot", "onEndOfSpeech");
			playStopSound();
			mActionbar.hideRecAnimation();
			isRecording = false;
		}

		@Override
		public void onError(int error) {
			Logger.info("Speech", "onError: " + error);
			if (recognizeRetry < MAX_RETRY_RECOGNIZE) {
				mActionbar.hideRecAnimation();
				recognizeRetry++;
				listenAgainWhenError();
				Log.d("MsRobot", "Error, try again: " + recognizeRetry);
			} else {
				Log.d("MsRobot", "Error, max retry");
				playErrorSound();
				mActionbar.hideRecAnimation();
				isRecording = false;
				switch (error) {
					case SpeechRecognizer.ERROR_AUDIO:
						showCenterToast(R.string.errorResultAudioError);
						break;
					case SpeechRecognizer.ERROR_CLIENT:
						showCenterToast(R.string.errorResultClientError);
						break;
					case SpeechRecognizer.ERROR_NETWORK:
						showCenterToast(R.string.errorResultNetworkError);
						break;
					case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
						showCenterToast(R.string.errorResultNetworkError);
						break;
					case SpeechRecognizer.ERROR_SERVER:
						showCenterToast(R.string.errorResultServerError);
						break;
					case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
						showCenterToast(R.string.errorResultServerError);
						break;
					case SpeechRecognizer.ERROR_NO_MATCH:
						showCenterToast(R.string.errorResultNoMatch);
						break;
					case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
						showCenterToast(R.string.errorResultTimeout);
						break;
					case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
						// This is programmer error.
						break;
					default:
						break;
				}
			}
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.d("MsRobot", "onEvent: " + eventType + " " + params);
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			Log.d("MsRobot", "onReadyForSpeech");
		}

		@Override
		public void onResults(Bundle results) {
			ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for (int i = 0; i < matches.size(); i++) {
				Logger.info("Speech", "text: " + matches.get(i));
			}
			if (mRecognizeVoiceListener != null) mRecognizeVoiceListener.onRecognize(matches);
			onRecognize(matches);
		}

		@Override
		public void onRmsChanged(float rmsdB) {
		}
	};

	/**
	 * prepare to listener for voice regconize
	 */
	public void listen() {
		recognizeRetry = 0;
		// play beep sound and sleep
		playStartSoundAndSleep();
		listenAgainWhenError();
	}

	public void stopListening() {
		recognizeRetry = MAX_RETRY_RECOGNIZE;
		if (mRecognize != null) {
			mRecognize.stopListening();
		}
	}

	public void listenAgainWhenError() {
		if (mRecognize == null) {
			showCenterToast(getString(R.string.err_NoDefaultRecognizer));
			return;
		}
		if (getTextToSpeech() != null) {
			getTextToSpeech().stop();
		}
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");

		isRecording = true;
		// set listener
		mRecognize.setRecognitionListener(mRecListener);
		mRecognize.startListening(recognizerIntent);
		mActionbar.showRecAnimation();
		Log.d("MsRobot", "listen");
	}

	/**
	 * Look up the default recognizer service in the preferences. If the default have not been set then set the first
	 * available recognizer as the default. If no recognizer is installed then return null.
	 */
	private ComponentName getServiceComponent() {
		String pkg = mSharePrefs.get(SharePrefs.PREF_RECOGNIZE_SERVICE_PACKAGE, null);
		String cls = mSharePrefs.get(SharePrefs.PREF_RECOGNIZE_SERVICE_CLASS, null);
		if (pkg == null || cls == null) {
			List<ResolveInfo> services = getPackageManager().queryIntentServices(
					new Intent(RecognitionService.SERVICE_INTERFACE), 0);
			if (services.isEmpty()) { return null; }
			ResolveInfo ri = services.iterator().next();
			pkg = ri.serviceInfo.packageName;
			cls = ri.serviceInfo.name;
			mSharePrefs.save(SharePrefs.PREF_RECOGNIZE_SERVICE_CLASS, cls);
			mSharePrefs.save(SharePrefs.PREF_RECOGNIZE_SERVICE_PACKAGE, pkg);
		}
		return new ComponentName(pkg, cls);
	}

	/**
	 * check for TTS exists
	 */
	private void checkTTS() {
		if (mSharePrefs.getCheckTTS()) {
			synchronized (setTextToSpeechLock) {
				mTts = new TextToSpeech(this, this);
				if (mTts == null) {
					Intent checkIntent = new Intent();
					checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
					startActivityForResult(checkIntent, RequestCode.REQUEST_CODE_CHECK_TTS);
				} else {
					syncVariable = true;
					setTextToSpeechLock.notifyAll();
				}
			}
		} else {
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, RequestCode.REQUEST_CODE_CHECK_TTS);
		}
	}

	/**
	 * init voice recognize
	 */
	private void initVoiceRecognizor() {

		ComponentName serviceComponent = getServiceComponent();
		if (serviceComponent == null) {
			installGoogleVoiceSearch(this);
		} else {
			mRecognize = SpeechRecognizer.createSpeechRecognizer(this, serviceComponent);
			if (mRecognize == null) {
				showCenterToast(getString(R.string.err_NoDefaultRecognizer));
			}
		}
	}

	/**
	 * Asking the permission for installing Google Voice Search. If permission granted sent user to Google Play
	 * 
	 * @param callerActivity
	 *            Activity, that initialized installing
	 */
	private static void installGoogleVoiceSearch(final Activity ownerActivity) {

		// creating a dialog asking user if he want
		// to install the Voice Search
		Dialog dialog = new AlertDialog.Builder(ownerActivity)
				.setMessage("For recognition its necessary to install \"Google Voice Search\"") // dialog message
				.setTitle("Install Voice Search from Google Play?") // dialog header
				.setPositiveButton("Install", new DialogInterface.OnClickListener() { // confirm button

							// Install Button click handler
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									// creating an Intent for opening applications page in Google Play
									// Voice Search package name: com.google.android.voicesearch
									Intent intent = new Intent(Intent.ACTION_VIEW, Uri
											.parse("market://details?id=com.google.android.voicesearch"));
									// setting flags to avoid going in application history (Activity call stack)
									intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
											| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
									// sending an Intent
									ownerActivity.startActivity(intent);
								} catch (Exception ex) {
									// if something going wrong
									// doing nothing
								}
							}
						})

				.setNegativeButton("Cancel", null) // cancel button
				.create();

		dialog.show(); // showing dialog
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

	public interface FragmentRecognizeVoiceListener {
		void onRecognize(ArrayList<String> data);
	}

	public void playStartSoundAndSleep() {
		playSound(R.raw.start);
		SystemClock.sleep(DELAY_AFTER_START_BEEP);
	}

	public void playStopSound() {
		playSound(R.raw.stop);
	}

	public void playErrorSound() {
		playSound(R.raw.error);
	}

	public CustomActionBar getCusomActionBar() {
		return mActionbar;
	}

	/**
	 * speak before recognize
	 * 
	 * @param msg
	 * @param id
	 */
	public void speakBeforeRecognize(String msg, int id) {
		if (getTextToSpeech() == null || isRecording) return;
		if (mSpeakAnimationListener != null) {
			mSpeakAnimationListener.startSpeakingAnimation();
		}
		isRecording = true;
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id + "");
		getTextToSpeech().speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	/**
	 * speak current again
	 * 
	 * @param msg
	 */
	public void speakBeforeRecognize(String msg) {
		if (getTextToSpeech() == null || isRecording) return;
		if (mSpeakAnimationListener != null) {
			mSpeakAnimationListener.startSpeakingAnimation();
		}
		isRecording = true;
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mModuleId + "");
		getTextToSpeech().speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	/**
	 * speak text
	 * 
	 * @param msg
	 * @param queueMode
	 */
	public void speak(String msg, int queueMode) {
		if (getTextToSpeech() == null || isRecording) return;
		if (mSpeakAnimationListener != null) {
			mSpeakAnimationListener.startSpeakingAnimation();
		}
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "0");
		getTextToSpeech().speak(msg, queueMode, myHashAlarm);
	}

	public void stopSpeak() {
		if (getTextToSpeech() == null) return;
		isRecording = false;
		getTextToSpeech().stop();
	}

	private void playSound(int sound) {
		MediaPlayer mp = MediaPlayer.create(this, sound);
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
		mp.start();
	}

	public interface SpeakAnimationListener {
		void startSpeakingAnimation();

		void stopSpeakingAnimation();
	}
}
