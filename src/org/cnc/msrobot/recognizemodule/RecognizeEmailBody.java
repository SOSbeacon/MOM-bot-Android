package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.activity.SendSmsEmailActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;

import android.app.Activity;
import android.content.Intent;

public class RecognizeEmailBody extends RecognizeBase implements RecognizeModuleListener {
	private Activity activity;

	public RecognizeEmailBody(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_EMAIL_BODY, activity.getString(R.string.recognize_email_body), activity
				.getString(R.string.recognize_email_body), null, true);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		Intent intent = new Intent(activity, SendSmsEmailActivity.class);
		intent.putExtra(SendSmsEmailActivity.EXTRA_TO, MainActivity.contactRecognize.email);
		intent.putExtra(SendSmsEmailActivity.EXTRA_BODY, data.get(0));
		activity.startActivity(intent);
	}
}
