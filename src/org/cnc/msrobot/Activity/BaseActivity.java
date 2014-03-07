package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.Logger;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
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
import android.view.Gravity;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class BaseActivity extends FragmentActivity implements OnInitListener, OnUtteranceCompletedListener {
	private static final int DELAY_AFTER_START_BEEP = 200;

	// Stop listening the user input after this period of milliseconds.
	// The input sentences are short and using the app should be snappy so
	// we don't want to spend too much on a single utterance.
	public static final int LISTENING_TIMEOUT = 4000;
	private final float SPEECH_RATE = 0.6f;
	protected Toast mToastCenter;
	protected SharePrefs mSharePrefs = SharePrefs.getInstance();
	protected RequestManager mRequestManager = RequestManager.getInstance();
	protected ProgressDialog mProgressDialog;
	protected TextToSpeech mTts;
	private final Object setTextToSpeechLock = new Object();
	private boolean syncVariable = false;
	private FragmentRecognizeVoiceListener mRecognizeVoiceListener;
	private SpeechRecognizer mRecognize;
	protected CustomActionBar mActionbar;
	final Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkTTS();

		mActionbar = new CustomActionBar(this);
		getActionBar().setCustomView(mActionbar);
		getActionBar().setDisplayShowCustomEnabled(true);
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.REQUEST_CODE_CHECK_TTS) {
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
		}
	}

	public void onRecognize(final ArrayList<String> data) {
	}

	public void setOnRecogizeVoiceListener(FragmentRecognizeVoiceListener listener) {
		mRecognizeVoiceListener = listener;
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
			mTts.setOnUtteranceCompletedListener(this);
		} else {
			showCenterToast("TTS engine not present");
		}
	}

	// It's callback
	public void onUtteranceCompleted(String utteranceId) {
	}

	/**
	 * prepare to listener for voice regconize
	 */
	public void listen() {
		Logger.info("Speech", "listen");
		// play beep sound and sleep
		playStartSoundAndSleep();
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

		// runnable and hanlder for stop listening
		final Runnable stopListening = new Runnable() {
			@Override
			public void run() {
				mRecognize.stopListening();
			}
		};

		// set listener
		mRecognize.setRecognitionListener(new RecognitionListener() {

			@Override
			public void onBeginningOfSpeech() {
				Logger.info("Speech", "onBeginningOfSpeech");
			}

			@Override
			public void onBufferReceived(byte[] buffer) {
			}

			@Override
			public void onEndOfSpeech() {
				handler.removeCallbacks(stopListening);
				playStopSound();
				mActionbar.hideRecAnimation();
			}

			@Override
			public void onError(int error) {
				Logger.info("Speech", "onError: " + error);
				handler.removeCallbacks(stopListening);
				playErrorSound();
				mActionbar.hideRecAnimation();
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
						showCenterToast(R.string.errorResultNoMatch);
						break;
					case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
						// This is programmer error.
						break;
					default:
						break;
				}
			}

			@Override
			public void onEvent(int eventType, Bundle params) {
				Logger.info("Speech", "onEvent: " + eventType + " " + params);
			}

			@Override
			public void onPartialResults(Bundle partialResults) {
			}

			@Override
			public void onReadyForSpeech(Bundle params) {
				handler.postDelayed(stopListening, LISTENING_TIMEOUT);
			}

			@Override
			public void onResults(Bundle results) {
				handler.removeCallbacks(stopListening);
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
		});
		mRecognize.startListening(recognizerIntent);
		mActionbar.showRecAnimation();
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
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, RequestCode.REQUEST_CODE_CHECK_TTS);
	}

	/**
	 * init voice recognize
	 */
	private void initVoiceRecognizor() {

		ComponentName serviceComponent = getServiceComponent();
		if (serviceComponent == null) {
			showCenterToast(getString(R.string.err_NoDefaultRecognizer));
		} else {
			mRecognize = SpeechRecognizer.createSpeechRecognizer(this, serviceComponent);
			if (mRecognize == null) {
				showCenterToast(getString(R.string.err_NoDefaultRecognizer));
			}
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

	public void showActionBarProgressBar() {
		mActionbar.showProgressBar();
	}

	public void hideActionBarProgressBar() {
		mActionbar.hideProgressBar();
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
}
