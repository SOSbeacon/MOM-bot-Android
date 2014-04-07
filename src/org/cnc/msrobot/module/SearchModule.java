package org.cnc.msrobot.module;

import org.cnc.msrobot.R;
import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.utils.AppUtils;

import android.content.Context;
import android.text.TextUtils;

public class SearchModule extends Module {
	public SearchModule(Context context, Input input, Output output) {
		super(context, input, output);
	}

	@Override
	public void onReceive(String data, String id) {
		if (TextUtils.isEmpty(data)) return;
		AppUtils.showGoogleSearchIntent(getContext(), data);
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
