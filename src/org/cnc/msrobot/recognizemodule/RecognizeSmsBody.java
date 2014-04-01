package org.cnc.msrobot.recognizemodule;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;
import org.cnc.msrobot.activity.SendSmsEmailActivity;
import org.cnc.msrobot.recognizemodule.RecognizeBase.RecognizeModuleListener;

import android.content.Intent;

public class RecognizeSmsBody extends RecognizeBase implements RecognizeModuleListener {
	private MainActivity activity;

	public RecognizeSmsBody(MainActivity activity) {
		super(activity, RecoginizeIds.MODULE_SMS_BODY, activity.getString(R.string.recognize_sms_body), activity
				.getString(R.string.recognize_sms_body), null, true);
		this.activity = activity;
		setListener(this);
	}

	@Override
	public void onRecoginze(final ArrayList<String> data) {
		Intent intent = new Intent(activity, SendSmsEmailActivity.class);
		intent.putExtra(SendSmsEmailActivity.EXTRA_TO, MainActivity.contactRecognize.phone);
		intent.putExtra(SendSmsEmailActivity.EXTRA_BODY, data.get(0));
		activity.startActivity(intent);
	}
}
