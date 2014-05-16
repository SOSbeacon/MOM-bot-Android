package org.cnc.mombot.utils;

import java.util.HashMap;
import java.util.Locale;

import org.cnc.mombot.utils.Consts.RequestCode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.view.Gravity;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class TextToSpeechUtils implements OnInitListener, OnUtteranceCompletedListener {

	private static TextToSpeechUtils instance = new TextToSpeechUtils();
	private static SpeechToText mStt = SpeechToText.getInstance();
	private TextToSpeech mTts;
	private SpeechListener mListenerModule;
	private SpeechListener mListenerAll;
	private Context context;
	private SharePrefs mSharePrefs = SharePrefs.getInstance();
	private boolean mInit = false;
	private boolean mStop = false;

	public static TextToSpeechUtils getInstance() {
		return instance;
	}

	public void init(Context activity) {
		this.context = activity;
		mTts = new TextToSpeech(context, this);
	}

	public void checkTTSData(Activity activity) {
		if (!mSharePrefs.getCheckTTS()) {
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			activity.startActivityForResult(checkIntent, RequestCode.REQUEST_CODE_CHECK_TTS);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// init TTS success, set language and speech rate
			mInit = true;
			if (mTts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) mTts.setLanguage(Locale.US);
			mTts.setSpeechRate(Consts.SPEECH_RATE);
			mTts.setOnUtteranceCompletedListener(this);
		} else {
			mTts = null;
			showCenterToast("TTS engine not present");
		}
	}

	public void shutdown() {
		if (getTextToSpeech() != null) {
			getTextToSpeech().shutdown();
		}
	}

	public boolean isSpeaking() {
		return getTextToSpeech().isSpeaking();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestCode.REQUEST_CODE_CHECK_TTS) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(context, this);
				if (mTts != null) {
					mSharePrefs.saveCheckTTS(true);
				}
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(installIntent);
			}
		}
	}

	/**
	 * set speak listener
	 * 
	 * @param listener
	 */
	public void setSpeechListenerForModule(SpeechListener listener) {
		mListenerModule = listener;
	}

	public void setSpeechListenerForAll(SpeechListener listener) {
		mListenerAll = listener;
	}

	public TextToSpeech getTextToSpeech() {
		if (mInit) {
			return mTts;
		} else {
			showCenterToast("TTS engine not init");
			return null;
		}
	}

	// It's callback
	public void onUtteranceCompleted(String utteranceId) {
		Logger.debug("TextToSpeech", "onUtteranceCompleted " + utteranceId);
		// check for manual stop
		if (mStop) {
			mStop = false;
			return;
		}
		// listener for activity
		if (mListenerAll != null) {
			mListenerAll.stopSpeech(utteranceId);
		}
		// listener for module
		if (mListenerModule != null) {
			if (!utteranceId.equals("0")) {
				mListenerModule.stopSpeech(utteranceId);
			}
		}
	}

	/**
	 * speak before recognize
	 * 
	 * @param msg
	 * @param id
	 */
	public void speakBeforeRecognize(String msg, String id) {
		Logger.debug("TextToSpeech", "speaking before recognize " + id);
		if (getTextToSpeech() == null) {
			if (mListenerModule != null) {
				mListenerModule.stopSpeech(id);
			}
			if (mListenerAll != null) {
				mListenerAll.stopSpeech(id);
			}
			return;
		}
		if (mStt.isRecording) return;
		if (mListenerModule != null) {
			mListenerModule.startSpeech(id);
		}
		if (mListenerAll != null) {
			mListenerAll.startSpeech(id);
		}
		mStop = false;
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
		getTextToSpeech().speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	public void speak(String msg, String id) {
		Logger.debug("TextToSpeech", "speaking " + id);
		if (getTextToSpeech() == null) {
			if (mListenerModule != null) {
				mListenerModule.stopSpeech(id);
			}
			if (mListenerAll != null) {
				mListenerAll.stopSpeech(id);
			}
			return;
		}
		if (mStt.isRecording) return;
		if (mListenerModule != null) {
			mListenerModule.startSpeech(id);
		}
		if (mListenerAll != null) {
			mListenerAll.startSpeech(id);
		}
		mStop = false;
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
		getTextToSpeech().speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	/**
	 * speak text
	 * 
	 * @param msg
	 * @param queueMode
	 */
	public void speak(String msg, int queueMode) {
		Logger.debug("TextToSpeech", "speaking 0");
		if (getTextToSpeech() == null) {
			if (mListenerModule != null) {
				mListenerModule.stopSpeech(null);
			}
			if (mListenerAll != null) {
				mListenerAll.stopSpeech(null);
			}
			return;
		}
		if (mStt.isRecording) return;
		if (mListenerModule != null) {
			mListenerModule.startSpeech("");
		}
		if (mListenerAll != null) {
			mListenerAll.startSpeech("");
		}
		mStop = false;
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "0");
		getTextToSpeech().speak(msg, queueMode, myHashAlarm);
	}

	public void stopSpeak() {
		if (getTextToSpeech() == null || !isSpeaking()) return;
		Logger.debug("TextToSpeech", "stop speaking manual");
		// listener for activity
		if (mListenerAll != null) {
			mListenerAll.stopSpeech("");
		}
		mStop = true;
		getTextToSpeech().stop();
	}

	private void showCenterToast(String message) {
		Toast mToastCenter = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		mToastCenter.setGravity(Gravity.CENTER, 0, 0);
		mToastCenter.show();
	}

	public interface SpeechListener {
		void startSpeech(String id);

		void stopSpeech(String id);
	}
}
