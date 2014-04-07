package org.cnc.msrobot.InputOutput;

import org.cnc.msrobot.utils.TextToSpeechUtils.SpeechListener;

public interface Output {
	public abstract void showGuide(String guide);

	public abstract void showAnswer(String result);

	public abstract void speak(String result, String id);

	public void setSpeechListener(SpeechListener listener);
}
