package org.cnc.mombot.inputoutput;

import org.cnc.mombot.activity.MainActivity;
import org.cnc.mombot.utils.TextToSpeechUtils.SpeechListener;

public class VoiceSecretaryOutput implements Output {
	private MainActivity mActivity;

	public VoiceSecretaryOutput(MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public void showGuide(String guide) {
		mActivity.addChatListView(guide, 0);
	}

	@Override
	public void showAnswer(String result) {
		mActivity.addChatListView(result, 1);
	}

	@Override
	public void speak(String result, String id) {
		mActivity.getTextToSpeech().speakBeforeRecognize(result, id);
	}

	@Override
	public void setSpeechListener(SpeechListener listener) {
		mActivity.getTextToSpeech().setSpeechListenerForModule(listener);
	}

}
