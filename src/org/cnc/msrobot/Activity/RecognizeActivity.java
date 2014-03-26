package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.List;

import org.cnc.msrobot.R;
import org.cnc.msrobot.utils.Logger;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class RecognizeActivity extends Activity implements OnClickListener {
	private static final int DELAY_RESTART_AFTER_ERROR = 200;
	private static final int DELAY_AFTER_START_BEEP = 200;
	private static final int DELAY_SHOW_RMSDB = 200;
	private static final int LISTENING_TIMEOUT = 8000;
	private SpeechRecognizer mRecognize;
	public static final String EXTRA_TEXT = "EXTRA_TEXT";
	private Toast mToastCenter;
	private SharePrefs mSharePrefs = SharePrefs.getInstance();
	private boolean isRecording = false;
	private EditText mEditText;
	private ImageButton mBtnMic;
	private Button mBtnComma, mBtnDot, mBtnDelete;
	private List<Drawable> mVolumeLevels;
	private float mCurrentRmsDb = 0;
	final Handler handler = new Handler();
	final Runnable runRestart = new Runnable() {

		@Override
		public void run() {
			// listen again
			if (isRecording) listen();
		}
	};
	final Runnable runVolumn = new Runnable() {
		@Override
		public void run() {
			if (mVolumeLevels != null) {
				int vol = Math.round(mCurrentRmsDb);
				// vol = -2 -> 10. I addition 2
				vol += 2;
				// vol / 2 -> vol level
				vol /= 2;
				if (vol > 6) vol = 6;
				else if (vol < 0) vol = 0;
				mBtnMic.setImageDrawable(mVolumeLevels.get(vol));
				handler.postDelayed(this, DELAY_SHOW_RMSDB);
			}
		}
	};

	// runnable and hanlder for stop listening
	final Runnable stopListening = new Runnable() {
		@Override
		public void run() {
			mRecognize.stopListening();
		}
	};
	final RecognitionListener mListener = new RecognitionListener() {

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
			handler.removeCallbacks(runVolumn);
		}

		@Override
		public void onError(int error) {
			Logger.info("Speech", "onError: " + error);
			handler.removeCallbacks(stopListening);
			handler.removeCallbacks(runVolumn);
			handler.postDelayed(runRestart, DELAY_RESTART_AFTER_ERROR);
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
			handler.post(runVolumn);
		}

		@Override
		public void onResults(Bundle results) {
			handler.removeCallbacks(stopListening);
			handler.removeCallbacks(runVolumn);
			ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for (int i = 0; i < matches.size(); i++) {
				Logger.info("Speech", "text: " + matches.get(i));
			}
			if (matches.size() > 0) {
				addText(" " + matches.get(0));
			}
			if (isRecording) listen();
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			mCurrentRmsDb = rmsdB;
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make us non-modal, so that others can receive touch events.
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);

		// ...but notify us that it happened.
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		setContentView(R.layout.activity_recognize);

		initVoiceRecognizor();

		mEditText = (EditText) findViewById(R.id.etRecognize);
		mBtnMic = (ImageButton) findViewById(R.id.btnMic);
		mBtnComma = (Button) findViewById(R.id.btnComma);
		mBtnDot = (Button) findViewById(R.id.btnDot);
		mBtnDelete = (Button) findViewById(R.id.btnDelete);

		mBtnMic.setOnClickListener(this);
		mBtnComma.setOnClickListener(this);
		mBtnDot.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);

		listen();

		// init volumn levels
		Resources mRes = getResources();
		mVolumeLevels = new ArrayList<Drawable>();
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level0));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level1));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level2));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level3));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level4));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level5));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level6));
	};

	/**
	 * prepare to listener for voice regconize
	 */
	public void listen() {
		Logger.info("Speech", "listen");
		isRecording = true;
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");

		// set listener
		mRecognize.setRecognitionListener(mListener);
		mRecognize.startListening(recognizerIntent);
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

	private void addText(String text) {
		mEditText.setText(mEditText.getText().toString() + text);
		mEditText.setSelection(mEditText.getText().length());
		mEditText.requestFocus();
	}

	private void deleteText() {
		String text = mEditText.getText().toString();
		if (text.length() == 0) return;
		int pos = text.length() - 1;
		while (pos > 0 && text.charAt(pos) != ' ') {
			pos--;
		}
		text = text.substring(0, pos);
		mEditText.setText(text);
		mEditText.setSelection(mEditText.getText().length());
		mEditText.requestFocus();
	}

	private void finishRec() {
		isRecording = false;
		mRecognize.stopListening();
		Intent intent = new Intent();
		intent.putExtra(EXTRA_TEXT, mEditText.getText().toString());
		setResult(Activity.RESULT_OK, intent);
	}

	@Override
	public void onBackPressed() {
		finishRec();
		super.onBackPressed();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// If we've received a touch notification that the user has touched
		// outside the app, finish the activity.
		if (MotionEvent.ACTION_OUTSIDE == event.getAction()) { return true; }

		// Delegate everything else to Activity.
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnMic:
				if (!isRecording) {
					// play beep sound and sleep
					playStartSoundAndSleep();
					listen();
				} else {
					playStopSound();
					isRecording = false;
					mRecognize.stopListening();
				}
				break;
			case R.id.btnComma:
				addText(",");
				break;
			case R.id.btnDot:
				addText(".");
				break;
			case R.id.btnDelete:
				deleteText();
				break;
			default:
				break;
		}
	}
}
