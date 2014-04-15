package org.cnc.msrobot.module;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cnc.msrobot.R;
import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.resource.StaticResource;
import org.cnc.msrobot.utils.AppUtils;

import android.content.Context;
import android.text.TextUtils;

public class CommandModule extends Module {
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
	public boolean onReceive(String data, String id) {
		if (TextUtils.isEmpty(data)) return false;
		boolean found = false;
		data = data.toLowerCase(Locale.US);
		if (data.startsWith(getResource().getString(R.string.command_send_message))
				|| data.startsWith(getResource().getString(R.string.command_send_email))) {
			getOutput().showAnswer(data);
			// get contact name
			String contactName = "";
			if (data.startsWith(getResource().getString(R.string.command_send_message))) {
				contactName = data.substring(getResource().getString(R.string.command_send_message).length());
			} else {
				contactName = data.substring(getResource().getString(R.string.command_send_email).length());
			}
			// check contact name
			int contactPosition = -1;
			// voice input, data is voice recognize
			if (!TextUtils.isEmpty(data) && StaticResource.listContact != null) {
				for (int i = 0; i < StaticResource.listContact.size(); i++) {
					String name = StaticResource.listContact.get(i).name.toLowerCase(Locale.US);
					if (contactName.toLowerCase(Locale.US).equals(name)) {
						getOutput().showAnswer(contactName);
						contactPosition = i;
						break;
					}
				}
			}
			if (contactPosition != -1) {
				found = true;
				new GetMessageContentModule(getContext(), getInput(), getOutput(), contactPosition).run();
			}
		} else if (data.startsWith(getResource().getString(R.string.command_search))) {
			getOutput().showAnswer(data);
			AppUtils.showGoogleSearchIntent(getContext(),
					data.substring(getResource().getString(R.string.command_search).length()));
			found = true;
		} else if (data.startsWith(getResource().getString(R.string.command_set_reminder))) {
			getOutput().showAnswer(data);
			// pattern for search number
			Pattern patternNumber = Pattern.compile("\\d+");
			Matcher m = patternNumber.matcher(data);
			Calendar calendar = Calendar.getInstance();
			// first number is hour
			if (m.find()) {
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group()));
			}
			// second number is minute
			if (m.find()) {
				calendar.set(Calendar.MINUTE, Integer.parseInt(m.group()));
			}
			AppUtils.setAlarm(getContext(), calendar);
			found = true;
		} else if (data.startsWith(getResource().getString(R.string.command_what_time))) {
			getOutput().showAnswer(data);
			getOutput().speak(AppUtils.getCurrentTimeForSpeech(getContext()), "");
			found = true;
		}
		if (!found) {
			getOutput().showAnswer(data);
		}
		return found;
	}

	@Override
	public void onFail(String id) {
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
	}
}
