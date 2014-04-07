package org.cnc.msrobot.InputOutput;

import java.util.ArrayList;

import org.cnc.msrobot.activity.BaseActivity;
import org.cnc.msrobot.utils.SpeechToText.SpeechToTextCallback;

import android.os.Handler;

public class VoiceInput implements Input, SpeechToTextCallback {
	private static final int DELAY_BEFORE_LISTEN = 500;
	private InputReceiveCallback callback;
	private String currentId;
	private Handler handler = new Handler();
	private BaseActivity mActivity;
	private Runnable mListenRunnable = new Runnable() {
		@Override
		public void run() {
			mActivity.getSpeechToText().listen();
		}
	};

	public VoiceInput(BaseActivity activity) {
		mActivity = activity;
		mActivity.getSpeechToText().setCallback(this);
	}

	@Override
	public void show(String id) {
		currentId = id;
		handler.postDelayed(mListenRunnable, DELAY_BEFORE_LISTEN);
	}

	@Override
	public void setReceiveCallback(InputReceiveCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onRecognize(ArrayList<String> data) {
		if (callback != null) {
			callback.onReceive(data.get(0), currentId);
		}
	}
}