package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.BaseActivity;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;
import org.cnc.msrobot.resource.ContactResource;

import android.speech.tts.TextToSpeech;

public class RecognizeEmailTo extends RecognizeBase implements RecognizeModuleListener {
	private MainActivity activity;

	public RecognizeEmailTo(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_EMAIL_TO, activity.getString(R.string.recognize_email_to), activity
				.getString(R.string.recognize_email_to), null, false);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		boolean found = false;
		for (String name : data) {
			name = name.toLowerCase(Locale.US);
			for (int i = 0; i < MainActivity.listContact.size(); i++) {
				ContactResource c = MainActivity.listContact.get(i);
				if (c.name.toLowerCase(Locale.US).contains(name)) {
					found = true;
					MainActivity.contactRecognize = c;
					activity.addChatListView(name, 1);
					break;
				}
			}
		}
		if (found) {
			activity.doRecognizeModule(RecoginizeIds.MODULE_EMAIL_SUBJECT);
		} else if (activity.recognizeRetry < BaseActivity.MAX_RETRY_RECOGNIZE) {
			activity.recognizeRetry++;
			activity.showCenterToast(activity.getString(R.string.msg_warn_no_people_recognize, activity.recognizeRetry));
			activity.speakBeforeRecognize(activity.getString(R.string.msg_warn_no_people_recognize,
					activity.recognizeRetry));
		} else {
			activity.showCenterToast(activity.getString(R.string.msg_err_dont_recognize));
			activity.speak(activity.getString(R.string.msg_err_dont_recognize), TextToSpeech.QUEUE_FLUSH);
		}

	}
}
