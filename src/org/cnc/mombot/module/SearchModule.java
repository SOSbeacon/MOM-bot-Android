package org.cnc.mombot.module;

import org.cnc.mombot.R;
import org.cnc.mombot.inputoutput.Input;
import org.cnc.mombot.inputoutput.Output;
import org.cnc.mombot.utils.AppUtils;

import android.content.Context;
import android.text.TextUtils;

public class SearchModule extends Module {
	public SearchModule(Context context, Input input, Output output) {
		super(context, input, output);
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (TextUtils.isEmpty(data)) return false;
		AppUtils.showGoogleSearchIntent(getContext(), data);
		return true;
	}

	@Override
	public void onFail(String data, String id) {
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
		getInput().show(id);
	}

	@Override
	public void run() {
		getOutput().showGuide(getResource().getString(R.string.recognize_search));
		getOutput().speak(getResource().getString(R.string.recognize_search), "");
	}
}
