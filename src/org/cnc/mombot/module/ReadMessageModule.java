package org.cnc.mombot.module;

import java.util.Locale;

import org.cnc.mombot.R;
import org.cnc.mombot.inputoutput.Input;
import org.cnc.mombot.inputoutput.Output;
import org.cnc.mombot.inputoutput.VoiceInput;
import org.cnc.mombot.inputoutput.YesNoInput;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

public class ReadMessageModule extends Module {
	private static final String STEP_ASK = "ask";
	private static final String STEP_READ = "read";
	private Handler handler = new Handler();
	private ReadMessageModuleListener listener;
	private String ask, content;
	private Input mYesNoInput;

	public ReadMessageModule(Context context, Input input, Output output) {
		super(context, input, output);
		if (input instanceof VoiceInput) {
			mYesNoInput = input;
		} else {
			mYesNoInput = new YesNoInput(context);
			mYesNoInput.setReceiveCallback(this);
		}
	}

	@Override
	public void run() {
	}

	public void readAsk(String askMessage, String contentMessage) {
		this.ask = askMessage;
		this.content = contentMessage;
		getOutput().showGuide(ask);
		getOutput().speak(ask, STEP_ASK);
	}

	public void setListener(ReadMessageModuleListener listener) {
		this.listener = listener;
	}

	public interface ReadMessageModuleListener {
		void onNext();
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (listener == null) return false;
		if (TextUtils.isEmpty(data)) {
			listener.onNext();
		} else {
			String yes = getResource().getString(R.string.common_yes).toLowerCase(Locale.US);
			if (yes.equals(data.toLowerCase(Locale.US))) {
				getOutput().speak(content, STEP_READ);
			} else {
				listener.onNext();
			}
			getOutput().showAnswer(data);
		}
		return true;
	}

	@Override
	public void onFail(String data, String id) {
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(final String id) {
		try {
			if (STEP_ASK.equals(id)) {
				mYesNoInput.show(id);
			} else if (STEP_READ.equals(id)) {
				// run listener for 200 ms delay
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (listener != null) listener.onNext();
					}
				}, 1000);
			}
		} catch (Exception e) {
		}
	}
}
