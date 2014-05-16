package org.cnc.mombot.inputoutput;

import java.util.ArrayList;

import org.cnc.mombot.utils.SpeechToText;
import org.cnc.mombot.utils.SpeechToText.SpeechToTextCallback;

import android.os.Handler;

public class VoiceInput implements Input, SpeechToTextCallback {
	private static final int DELAY_BEFORE_LISTEN = 500;
	private InputReceiveCallback callback;
	private String currentId;
	private Handler handler = new Handler();
	private SpeechToText mStt = SpeechToText.getInstance();
	private Runnable mListenRunnable = new Runnable() {
		@Override
		public void run() {
			mStt.listen();
		}
	};

	public VoiceInput() {
	}

	@Override
	public void show(String id) {
		currentId = id;
		handler.postDelayed(mListenRunnable, DELAY_BEFORE_LISTEN);
	}

	@Override
	public void setReceiveCallback(InputReceiveCallback callback) {
		this.callback = callback;
		mStt.setCallback(this);
	}

	@Override
	public void onRecognize(ArrayList<String> data) {
		if (callback != null) {
			for (String s : data) {
				if (callback.onReceive(s, currentId)) { return; }
			}
			callback.onFail(data.get(0), currentId);
		}
	}
}