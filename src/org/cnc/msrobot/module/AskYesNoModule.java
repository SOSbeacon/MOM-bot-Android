package org.cnc.msrobot.module;

import java.util.Locale;

import org.cnc.mombot.R;
import org.cnc.msrobot.inputoutput.Input;
import org.cnc.msrobot.inputoutput.Output;
import org.cnc.msrobot.inputoutput.VoiceInput;
import org.cnc.msrobot.inputoutput.YesNoInput;

import android.content.Context;

public class AskYesNoModule extends Module {
	private int askMessageId;
	private AskYesNoModuleListener listener;
	private Input mYesNoInput;

	public AskYesNoModule(Context context, Input input, Output output, int askMessageId, AskYesNoModuleListener listener) {
		super(context, input, output);
		if (input instanceof VoiceInput) {
			mYesNoInput = input;
		} else {
			mYesNoInput = new YesNoInput(context);
			mYesNoInput.setReceiveCallback(this);
		}
		this.askMessageId = askMessageId;
		this.listener = listener;
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (listener == null || data == null) return false;
		String yes = getResource().getString(R.string.common_yes).toLowerCase(Locale.US);
		if (yes.equals(data.toLowerCase(Locale.US))) {
			listener.onYes();
		} else {
			listener.onNo();
		}
		getOutput().showAnswer(data);
		return true;
	}

	@Override
	public void onFail(String data, String id) {
	}

	@Override
	public void run() {
		getOutput().showGuide(getResource().getString(askMessageId));
		getOutput().speak(getResource().getString(askMessageId), "");
	}

	public interface AskYesNoModuleListener {
		void onYes();

		void onNo();
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
		mYesNoInput.show(id);
	}
}
