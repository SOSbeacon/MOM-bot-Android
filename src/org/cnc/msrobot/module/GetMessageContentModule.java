package org.cnc.msrobot.module;

import org.cnc.mombot.R;
import org.cnc.msrobot.activity.RecognizeActivity;
import org.cnc.msrobot.activity.SendSmsEmailActivity;
import org.cnc.msrobot.inputoutput.Input;
import org.cnc.msrobot.inputoutput.Output;
import org.cnc.msrobot.inputoutput.VoiceInput;
import org.cnc.msrobot.resource.StaticResource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

public class GetMessageContentModule extends Module {
	private static final String STEP_CONTENT = "content";
	private static final String STEP_ERROR = "error";
	private int contactPosition;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0 && msg.obj instanceof String) {
				String text = (String) msg.obj;
				onReceive(text, STEP_CONTENT);
			}
		}
	};

	public GetMessageContentModule(Context context, Input input, Output output, int contactPosition) {
		super(context, input, output);
		this.contactPosition = contactPosition;
	}

	@Override
	public void run() {
		if (StaticResource.listContact == null || StaticResource.listContact.size() == 0) {
			getOutput().showGuide(getResource().getString(R.string.msg_war_no_contact));
			return;
		}
		// show guide and speech request name of contact whom you want to send message
		getOutput().showGuide(getResource().getString(R.string.recognize_message_content));
		getOutput().speak(getResource().getString(R.string.recognize_message_content), STEP_CONTENT);
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (STEP_CONTENT.equals(id)) {
			Intent intent = new Intent(getContext(), SendSmsEmailActivity.class);
			intent.putExtra(SendSmsEmailActivity.EXTRA_CONTACT_POSITION, contactPosition);
			intent.putExtra(SendSmsEmailActivity.EXTRA_BODY, data);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
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
	public void stopSpeech(String id) {
		// if step is error step, abandon
		if (STEP_ERROR.equals(id)) { return; }

		// if step is message content step, set null
		if (getInput() instanceof VoiceInput) {
			Intent intent = new Intent(getContext(), RecognizeActivity.class);
			intent.putExtra(RecognizeActivity.EXTRA_HANDLER, new Messenger(handler));
			getContext().startActivity(intent);
		} else {
			getInput().show(id);
		}
	}
}
