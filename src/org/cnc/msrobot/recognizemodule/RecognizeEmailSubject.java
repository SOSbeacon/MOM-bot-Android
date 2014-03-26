package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;

public class RecognizeEmailSubject extends RecognizeBase implements RecognizeModuleListener {
	private MainActivity activity;

	public RecognizeEmailSubject(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_EMAIL_SUBJECT, activity.getString(R.string.recognize_email_subject),
				activity.getString(R.string.recognize_email_subject), null, true);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		MainActivity.subjectRecognize = data.get(0);
		activity.addChatListView(MainActivity.subjectRecognize, 1);
		activity.doRecognizeModule(RecoginizeIds.MODULE_EMAIL_BODY);
	}
}
