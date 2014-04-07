package org.cnc.msrobot.module;

import org.cnc.msrobot.R;
import org.cnc.msrobot.InputOutput.ContactListInput;
import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.InputOutput.VoiceInput;
import org.cnc.msrobot.activity.SendSmsEmailActivity;
import org.cnc.msrobot.resource.StaticResource;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class SendMessageModule extends Module {
	private static final String STEP_CONTACT = "contact";
	private static final String STEP_CONTENT = "content";
	private static final String STEP_ERROR = "error";
	private int contactPosition;
	private Input mContactInput;

	public SendMessageModule(Context context, Input input, Output output) {
		super(context, input, output);
		if (input instanceof VoiceInput) {
			mContactInput = input;
		} else {
			mContactInput = new ContactListInput(context);
			mContactInput.setReceiveCallback(this);
		}
	}

	@Override
	public void run() {
		if (StaticResource.listContact == null || StaticResource.listContact.size() == 0) {
			getOutput().showGuide(getResource().getString(R.string.msg_war_no_contact));
			return;
		}
		// show guide and speech request name of contact whom you want to send message
		getOutput().showGuide(getResource().getString(R.string.recognize_message_to));
		getOutput().speak(getResource().getString(R.string.recognize_message_to), STEP_CONTACT);
	}

	@Override
	public void onReceive(String data, String id) {
		if (TextUtils.isEmpty(data)) return;
		if (STEP_CONTACT.equals(id)) {
			// default current position = -1, if found, it will be > -1
			contactPosition = -1;
			try {
				contactPosition = Integer.parseInt(data);
			} catch (Exception ex) {
				contactPosition = -1;
			}
			if (contactPosition == -1) {
				// not found in list contact
				getOutput().showGuide(getResource().getString(R.string.msg_warn_no_people_recognize, data));
				getOutput().speak(getResource().getString(R.string.msg_warn_no_people_recognize, data), STEP_ERROR);
			} else {
				// do next step for message content
				getOutput().showGuide(getResource().getString(R.string.recognize_message_content));
				getOutput().speak(getResource().getString(R.string.recognize_message_content), STEP_CONTENT);
			}
		} else if (STEP_CONTENT.equals(id)) {
			Intent intent = new Intent(getContext(), SendSmsEmailActivity.class);
			intent.putExtra(SendSmsEmailActivity.EXTRA_CONTACT_POSITION, contactPosition);
			intent.putExtra(SendSmsEmailActivity.EXTRA_BODY, data);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
		}
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
		// if step is error step, abandon
		if (STEP_ERROR.equals(id)) { return; }

		if (STEP_CONTACT.equals(id)) {
			// if step is message contact step, we will use Contact input
			mContactInput.show(id);
		} else {
			// if step is message content step, set null
			getInput().show(id);
		}
	}
}
