package org.cnc.mombot.module;

import java.util.Locale;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.RecognizeActivity;
import org.cnc.mombot.activity.SendSmsEmailActivity;
import org.cnc.mombot.inputoutput.ContactListInput;
import org.cnc.mombot.inputoutput.Input;
import org.cnc.mombot.inputoutput.Output;
import org.cnc.mombot.inputoutput.VoiceInput;
import org.cnc.mombot.resource.StaticResource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

public class SendMessageModule extends Module {
	private static final String STEP_CONTACT = "contact";
	private static final String STEP_CONTENT = "content";
	private static final String STEP_ERROR = "error";
	private int contactPosition;
	private Input mContactInput;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				String text = "";
				if (msg.obj instanceof String) {
					text = (String) msg.obj;
				}
				onReceive(text, STEP_CONTENT);
			}
		}
	};

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
	public boolean onReceive(String data, String id) {
		if (STEP_CONTACT.equals(id)) {
			if (TextUtils.isEmpty(data)) return false;
			// default current position = -1, if found, it will be > -1
			contactPosition = -1;
			if (getInput() instanceof VoiceInput) {
				// voice input, data is voice recognize
				if (!TextUtils.isEmpty(data) && StaticResource.listContact != null) {
					for (int i = 0; i < StaticResource.listContact.size(); i++) {
						String name = StaticResource.listContact.get(i).name.toLowerCase(Locale.US);
						if (data.toLowerCase(Locale.US).equals(name)) {
							getOutput().showAnswer(data);
							contactPosition = i;
							break;
						}
					}
				}
			} else {
				// text input, select contact from list, data is position of contact
				try {
					contactPosition = Integer.parseInt(data);
				} catch (Exception ex) {
					contactPosition = -1;
				}
			}
			if (contactPosition == -1) {
				return false;
			} else {
				// do next step for message content
				getOutput().showGuide(getResource().getString(R.string.recognize_message_content));
				getOutput().speak(getResource().getString(R.string.recognize_message_content), STEP_CONTENT);
				return true;
			}
		} else if (STEP_CONTENT.equals(id)) {
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
		if (STEP_CONTACT.equals(id)) {
			// not found in list contact
			getOutput().showGuide(getResource().getString(R.string.msg_warn_no_people_recognize, data));
			getOutput().showAnswer(data);
			getOutput().speak(getResource().getString(R.string.msg_warn_no_people_recognize, data), STEP_ERROR);
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
			if (getInput() instanceof VoiceInput) {
				Intent intent = new Intent(getContext(), RecognizeActivity.class);
				intent.putExtra(RecognizeActivity.EXTRA_HANDLER, new Messenger(handler));
				getContext().startActivity(intent);
			} else {
				getInput().show(id);
			}
		}
	}
}
