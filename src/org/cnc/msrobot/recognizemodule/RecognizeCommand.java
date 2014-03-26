package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;

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
			if (cmd.equals(getResource().getString(R.string.command_send_text_message))) {
				activity.addChatListView(cmd, 1);
				activity.doRecognizeModule(RecoginizeIds.MODULE_SMS_TO);
				found = true;
				break;
			}
		}
		if (!found) {
			activity.addChatListView(data.get(0), 1);
		}
	}
}
