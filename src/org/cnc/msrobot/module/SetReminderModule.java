package org.cnc.msrobot.module;

import java.text.ParseException;
import java.util.Date;

import org.cnc.msrobot.R;
import org.cnc.msrobot.InputOutput.DateTimeInput;
import org.cnc.msrobot.InputOutput.Input;
import org.cnc.msrobot.InputOutput.Output;
import org.cnc.msrobot.InputOutput.VoiceInput;
import org.cnc.msrobot.activity.ReminderAddActivity;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.DateTimeFormater;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class SetReminderModule extends Module {
	private static final String STEP_NAME = "name";
	private static final String STEP_DESC = "desc";
	private static final String STEP_START_TIME = "start_time";
	private static final String STEP_END_TIME = "end_time";
	private Input mDateTimeInput;
	private String mName, mDesc;
	private Date mStartTime, mEndTime;

	public SetReminderModule(Context context, Input input, Output output) {
		super(context, input, output);
		if (input instanceof VoiceInput) {
			mDateTimeInput = input;
		} else {
			mDateTimeInput = new DateTimeInput(context);
			mDateTimeInput.setReceiveCallback(this);
		}
	}

	@Override
	public void run() {
		// show guide and speech request name of reminder
		getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_name));
		getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_name), STEP_NAME);
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (TextUtils.isEmpty(data)) return false;
		if (STEP_NAME.equals(id)) {
			mName = data;
			// do next step for message content
			getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_description));
			getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_description), STEP_DESC);
		} else if (STEP_DESC.equals(id)) {
			mDesc = data;
			// do next step for message content
			getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_start_time));
			getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_start_time), STEP_START_TIME);
		} else if (STEP_START_TIME.equals(id)) {
			try {
				mStartTime = DateTimeFormater.timeServerFormat.parse(data);
				// do next step for message content
				getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_end_time));
				getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_end_time), STEP_END_TIME);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (STEP_END_TIME.equals(id)) {
			try {
				mEndTime = DateTimeFormater.timeServerFormat.parse(data);
				Intent intent = new Intent(getContext(), ReminderAddActivity.class);
				intent.putExtra(Consts.PARAMS_EVENT_TITLE, mName);
				intent.putExtra(Consts.PARAMS_EVENT_CONTENT, mDesc);
				intent.putExtra(Consts.PARAMS_EVENT_START_TIME, mStartTime.getTime());
				intent.putExtra(Consts.PARAMS_EVENT_END_TIME, mEndTime.getTime());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void onFail(String id) {
	}

	@Override
	public void startSpeech(String id) {
	}

	@Override
	public void stopSpeech(String id) {
		if (STEP_NAME.equals(id) || STEP_DESC.equals(id)) {
			// if step is message contact step, we will use Contact input
			getInput().show(id);
		} else if (STEP_START_TIME.equals(id) || STEP_END_TIME.equals(id)) {
			// if step is message content step, set null
			mDateTimeInput.show(id);
		}
	}
}
