package org.cnc.msrobot.module;

import java.util.Locale;

import org.cnc.mombot.R;
import org.cnc.msrobot.activity.CalendarActivity;
import org.cnc.msrobot.activity.ReadEmailSmsActivity;
import org.cnc.msrobot.activity.ReminderAddActivity;
import org.cnc.msrobot.inputoutput.Input;
import org.cnc.msrobot.inputoutput.Output;
import org.cnc.msrobot.resource.StaticResource;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

public class CommandModule extends Module {
	private static final int DELAY_SHOW_ANSWER = 1000;
	private Handler handler = new Handler();

	public CommandModule(Context context, Input input, Output output) {
		super(context, input, output);
	}

	@Override
	public void run() {
		// show guide for command example, not speak
		getOutput().showGuide(getResource().getString(R.string.command_example));
		// show input
		getInput().show("");
	}

	@Override
	public boolean onReceive(String datas, String id) {
		if (TextUtils.isEmpty(datas)) return false;
		boolean found = false;
		final String data = datas.toLowerCase(Locale.US);
		if (data.equals(getResource().getString(R.string.command_send_message))
				|| data.equals(getResource().getString(R.string.command_send_email))) {
			ModuleManager.getInstance().runModule(Module.MODULE_SEND_MESSAGE);
			getOutput().showAnswer(data);
			found = true;
		} else if (data.equals(getResource().getString(R.string.command_set_reminder))) {
			ModuleManager.getInstance().runModule(Module.MODULE_SET_REMINDER);
			getOutput().showAnswer(data);
			found = true;
		} else if (data.startsWith(getResource().getString(R.string.command_send_message_to))
				|| data.startsWith(getResource().getString(R.string.command_send_email_to))) {
			getOutput().showAnswer(data);
			// get contact name
			String contactName = "";
			int len;
			if (data.startsWith(getResource().getString(R.string.command_send_message_to))) {
				len = getResource().getString(R.string.command_send_message_to).length() + 1;
			} else {
				len = getResource().getString(R.string.command_send_email_to).length() + 1;
			}
			if (data.length() > len) {
				contactName = data.substring(len);
				// check contact name
				int contactPosition = -1;
				// voice input, data is voice recognize
				if (!TextUtils.isEmpty(data) && StaticResource.listContact != null) {
					for (int i = 0; i < StaticResource.listContact.size(); i++) {
						String name = StaticResource.listContact.get(i).name.toLowerCase(Locale.US);
						if (contactName.toLowerCase(Locale.US).equals(name)) {
							getOutput().showAnswer(data);
							contactPosition = i;
							break;
						}
					}
				}
				if (contactPosition != -1) {
					found = true;
					new GetMessageContentModule(getContext(), getInput(), getOutput(), contactPosition).run();
				}
			}
		} else if (data.startsWith(getResource().getString(R.string.command_search))) {
			getOutput().showAnswer(data);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					AppUtils.showGoogleSearchIntent(getContext(),
							data.substring(getResource().getString(R.string.command_search).length()));
				}
			}, DELAY_SHOW_ANSWER);
			found = true;
		} else if (data.startsWith(getResource().getString(R.string.command_set_reminder))) {
			int len = getResource().getString(R.string.command_set_reminder).length() + 1;
			if (data.length() > len) {
				final String title = data.substring(len);
				getOutput().showAnswer(data);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(getContext(), ReminderAddActivity.class);
						intent.putExtra(Consts.PARAMS_EVENT_TITLE, title);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getContext().startActivity(intent);
					}
				}, DELAY_SHOW_ANSWER);
				found = true;
			}
		} else if (data.startsWith(getResource().getString(R.string.command_what_time))) {
			getOutput().showAnswer(data);
			getOutput().speak(AppUtils.getCurrentTimeForSpeech(getContext()), "");
			found = true;
		} else if (data.contains(getResource().getString(R.string.command_calendar))) {
			getOutput().showAnswer(data);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getContext(), CalendarActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
				}
			}, DELAY_SHOW_ANSWER);
			found = true;
		} else if (data.contains(getResource().getString(R.string.command_read_message))) {
			getOutput().showAnswer(data);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getContext(), ReadEmailSmsActivity.class);
					intent.putExtra(ReadEmailSmsActivity.EXTRA_TYPE, ReadEmailSmsActivity.TYPE_SENT_SMS);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
				}
			}, DELAY_SHOW_ANSWER);
			found = true;
		} else if (data.contains(getResource().getString(R.string.command_read_email))) {
			getOutput().showAnswer(data);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getContext(), ReadEmailSmsActivity.class);
					intent.putExtra(ReadEmailSmsActivity.EXTRA_TYPE, ReadEmailSmsActivity.TYPE_SENT_EMAIL);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
				}
			}, DELAY_SHOW_ANSWER);
			found = true;
		}
		return found;
	}

	@Override
	public void onFail(String data, String id) {
		getOutput().showAnswer(data);
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
	}
}
