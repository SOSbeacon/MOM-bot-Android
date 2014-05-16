package org.cnc.mombot.utils;

import org.cnc.mombot.activity.MainActivity;
import org.cnc.mombot.requestmanager.RequestManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class ListenToPhoneState extends PhoneStateListener {
	private boolean phonecall = false;
	private AudioUtils audio;
	private Context context;

	public ListenToPhoneState(Context context) {
		this.context = context;
	}

	public void onCallStateChanged(int state, String incomingNumber) {
		switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (phonecall) {
					// change state from offhook to idle
					if (audio != null && audio.isRecording()) {
						audio.stopRecord();
						Logger.debug("Emergency", "stop record: " + audio.getRecordFilePath());
						// request update emergency
						String id = SharePrefs.getInstance().getEmergencyId();
						if (!TextUtils.isEmpty(id)) {
							Bundle bundle = new Bundle();
							bundle.putString(Consts.PARAMS_MESSAGE_AUDIO, audio.getRecordFilePath());
							bundle.putString(Consts.PARAMS_ID, id);
							RequestManager.getInstance().request(Actions.ACTION_UPDATE_EMERGENCY, bundle, null, null);
						}
						// start activity again
						Intent intent = new Intent(context, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra(MainActivity.EXTRA_RECORD_FILE, audio.getRecordFilePath());
						context.startActivity(intent);
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (!phonecall) {
					// make phone call, start record
					if (audio == null) {
						audio = new AudioUtils();
					}
					audio.startRecord();
					phonecall = true;
					Logger.debug("Emergency", "start recording");
				}
				break;
		}
	}
}
