package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;
import org.cnc.msrobot.utils.AppUtils;

import android.speech.tts.TextToSpeech;

public class RecognizeCommand extends RecognizeBase implements RecognizeModuleListener {
	private MainActivity activity;

	public RecognizeCommand(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_COMMAND, null, activity.getString(R.string.command_example), null, false);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		boolean found = false;
		for (String cmd : data) {
			cmd = cmd.toLowerCase(Locale.US);
			if (cmd.contains(getResource().getString(R.string.command_send))
					&& cmd.contains(getResource().getString(R.string.command_message))) {
				activity.addChatListView(cmd, 1);
				activity.doRecognizeModule(RecoginizeIds.MODULE_SMS_TO);
				found = true;
				break;
			} else if (cmd.contains(getResource().getString(R.string.command_send))
					&& cmd.contains(getResource().getString(R.string.command_email))) {
				activity.addChatListView(cmd, 1);
				activity.doRecognizeModule(RecoginizeIds.MODULE_EMAIL_TO);
				found = true;
				break;
			} else if (cmd.startsWith(getResource().getString(R.string.command_search))) {
				activity.addChatListView(cmd, 1);
				AppUtils.showGoogleSearchIntent(activity,
						cmd.substring(getResource().getString(R.string.command_search).length()));
				found = true;
				break;
			} else if (cmd.startsWith(getResource().getString(R.string.command_set_alarm))) {
				activity.addChatListView(cmd, 1);
				// pattern for search number
				Pattern patternNumber = Pattern.compile("\\d+");
				Matcher m = patternNumber.matcher(cmd);
				Calendar calendar = Calendar.getInstance();
				// first number is hour
				if (m.find()) {
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group()));
				}
				// second number is minute
				if (m.find()) {
					calendar.set(Calendar.MINUTE, Integer.parseInt(m.group()));
				}
				AppUtils.setAlarm(activity, calendar);
				found = true;
				break;
			} else if (cmd.startsWith(getResource().getString(R.string.command_what_time))) {
				activity.addChatListView(cmd, 1);
				activity.speak(AppUtils.getCurrentTimeForSpeech(activity), TextToSpeech.QUEUE_FLUSH);
				found = true;
				break;
			}
		}
		if (!found) {
			activity.addChatListView(data.get(0), 1);
		}
	}
}
