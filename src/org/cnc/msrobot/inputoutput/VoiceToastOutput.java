package org.cnc.msrobot.inputoutput;

import org.cnc.msrobot.activity.BaseActivity;
import org.cnc.msrobot.utils.TextToSpeechUtils.SpeechListener;

import android.os.Handler;

public class VoiceToastOutput implements Output {
	private BaseActivity mActivity;
	private Handler handler = new Handler();

	public VoiceToastOutput(BaseActivity activity) {
		mActivity = activity;
	}

	@Override
	public void showGuide(String guide) {
		mActivity.showCenterToast(guide);
	}

	@Override
	public void showAnswer(String result) {
		mActivity.showCenterToast(result);
	}

	@Override
	public void speak(final String result, final String id) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				mActivity.getTextToSpeech().speak(result, id);
			}
		});
	}

	@Override
	public void setSpeechListener(SpeechListener listener) {
		mActivity.getTextToSpeech().setSpeechListenerForModule(listener);
	}

}
