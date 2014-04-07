package org.cnc.msrobot.utils;

import java.util.ArrayList;
import java.util.List;

import org.cnc.msrobot.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SpeechToText {
	private static final int DELAY_AFTER_START_BEEP = 100;
	private static final int MAX_RETRY_RECOGNIZE = 3;
	private static SpeechToText instance = new SpeechToText();
	private TextToSpeechUtils mTts = TextToSpeechUtils.getInstance();
	private int recognizeRetry = 0;
	public boolean isRecording = false;
	private SpeechRecognizer mRecognize;
	private Context context;
	private SpeechToTextCallback callback;
	private SpeechToTextListener listener;

	public static SpeechToText getInstance() {
		return instance;
	}

	public void init(Context context) {
		this.context = context;
		initVoiceRecognizor();
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
			if (listener != null) listener.onSpeechStop();
			isRecording = false;
		}

		@Override
		public void onError(int error) {
			Logger.info("Speech", "onError: " + error);
			if (recognizeRetry < MAX_RETRY_RECOGNIZE) {
				if (listener != null) listener.onSpeechStop();
				recognizeRetry++;
				listenAgainWhenError();
				Log.d("MsRobot", "Error, try again: " + recognizeRetry);
			} else {
				Log.d("MsRobot", "Error, max retry");
				playErrorSound();
				if (listener != null) listener.onSpeechStop();
				isRecording = false;
				mRecognize.setRecognitionListener(null);
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
						// showCenterToast(R.string.errorResultNoMatch);
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
			if (callback != null) callback.onRecognize(matches);
		}

		@Override
		public void onRmsChanged(float rmsdB) {
		}
	};

	/**
	 * set speech to text callback for receiving data
	 * 
	 * @param callback
	 */
	public void setCallback(SpeechToTextCallback callback) {
		this.callback = callback;
	}

	/**
	 * set speech to text listener (start listen, end listen)
	 * 
	 * @param listener
	 */
	public void setListener(SpeechToTextListener listener) {
		this.listener = listener;
	}

	/**
	 * prepare to listener for voice recognize
	 */
	public void listen() {
		if (mRecognize == null) {
			showCenterToast(context.getString(R.string.err_NoDefaultRecognizer));
			return;
		}
		recognizeRetry = 0;
		// play beep sound and sleep
		playStartSoundAndSleep();
		// set listener
		mRecognize.setRecognitionListener(mRecListener);
		// start recognize
		listenAgainWhenError();
	}

	public void stopListening() {
		recognizeRetry = MAX_RETRY_RECOGNIZE;
		if (mRecognize == null) {
			showCenterToast(context.getString(R.string.err_NoDefaultRecognizer));
			return;
		}
		mRecognize.stopListening();
	}

	public void listenAgainWhenError() {
		if (mRecognize == null) {
			isRecording = false;
			showCenterToast(context.getString(R.string.err_NoDefaultRecognizer));
			return;
		}
		// stop speech
		mTts.stopSpeak();
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getApplicationContext()
				.getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");

		isRecording = true;
		// set listener
		mRecognize.startListening(recognizerIntent);
		if (listener != null) listener.onSpeechStart();
		Log.d("MsRobot", "listen");
	}

	private void showCenterToast(String message) {
		Toast mToastCenter = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		mToastCenter.setGravity(Gravity.CENTER, 0, 0);
		mToastCenter.show();
	}

	/**
	 * init voice recognize
	 */
	private void initVoiceRecognizor() {

		ComponentName serviceComponent = getServiceComponent();
		if (serviceComponent != null) {
			mRecognize = SpeechRecognizer.createSpeechRecognizer(context, serviceComponent);
			if (mRecognize == null) {
				showCenterToast(R.string.err_NoDefaultRecognizer);
			}
		}
	}

	/**
	 * Asking the permission for installing Google Voice Search. If permission granted sent user to Google Play
	 * 
	 * @param callerActivity
	 *            Activity, that initialized installing
	 */
	public void checkSTT(final Context context) {
		ComponentName serviceComponent = getServiceComponent();
		if (serviceComponent != null) return;
		// creating a dialog asking user if he want
		// to install the Voice Search
		Dialog dialog = new AlertDialog.Builder(context)
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
									context.startActivity(intent);
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
	 * Look up the default recognizer service in the preferences. If the default have not been set then set the first
	 * available recognizer as the default. If no recognizer is installed then return null.
	 */
	private ComponentName getServiceComponent() {
		SharePrefs sharePrefs = SharePrefs.getInstance();
		String pkg = sharePrefs.get(SharePrefs.PREF_RECOGNIZE_SERVICE_PACKAGE, null);
		String cls = sharePrefs.get(SharePrefs.PREF_RECOGNIZE_SERVICE_CLASS, null);
		if (pkg == null || cls == null) {
			List<ResolveInfo> services = context.getPackageManager().queryIntentServices(
					new Intent(RecognitionService.SERVICE_INTERFACE), 0);
			if (services.isEmpty()) { return null; }
			ResolveInfo ri = services.iterator().next();
			pkg = ri.serviceInfo.packageName;
			cls = ri.serviceInfo.name;
			sharePrefs.save(SharePrefs.PREF_RECOGNIZE_SERVICE_CLASS, cls);
			sharePrefs.save(SharePrefs.PREF_RECOGNIZE_SERVICE_PACKAGE, pkg);
		}
		return new ComponentName(pkg, cls);
	}

	private void playStartSoundAndSleep() {
		playSound(R.raw.start);
		SystemClock.sleep(DELAY_AFTER_START_BEEP);
	}

	private void playStopSound() {
		playSound(R.raw.stop);
	}

	private void playErrorSound() {
		playSound(R.raw.error);
	}

	private void playSound(int sound) {
		MediaPlayer mp = MediaPlayer.create(context, sound);
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
		mp.start();
	}

	private void showCenterToast(int resId) {
		showCenterToast(context.getString(resId));
	}

	public interface SpeechToTextCallback {
		void onRecognize(ArrayList<String> data);
	}

	public interface SpeechToTextListener {
		void onSpeechStart();

		void onSpeechStop();
	}
}
