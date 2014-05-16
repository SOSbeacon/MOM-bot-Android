package org.cnc.mombot.module;

import java.util.Calendar;
import java.util.Date;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.ReminderAddActivity;
import org.cnc.mombot.inputoutput.DateTimeInput;
import org.cnc.mombot.inputoutput.Input;
import org.cnc.mombot.inputoutput.Output;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.DateTimeFormater;

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
		mDateTimeInput = new DateTimeInput(context);
		mDateTimeInput.setReceiveCallback(this);
	}

	@Override
	public void run() {
		// show guide and speech request name of reminder
		getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_name));
		getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_name), STEP_NAME);
	}

	@Override
	public boolean onReceive(String data, String id) {
		if (STEP_NAME.equals(id)) {
			if (TextUtils.isEmpty(data)) return false;
			mName = data;
			getOutput().showAnswer(mName);
			// do next step for message content
			getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_description));
			getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_description), STEP_DESC);
		} else if (STEP_DESC.equals(id)) {
			mDesc = data;
			getOutput().showAnswer(mDesc);
			// do next step for message content
			getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_start_time));
			getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_start_time), STEP_START_TIME);
		} else if (STEP_START_TIME.equals(id)) {
			try {
				mStartTime = DateTimeFormater.timeServerFormat.parse(data);
				// do next step for message content
				getOutput().showGuide(getResource().getString(R.string.recognize_ask_reminder_end_time));
				getOutput().speak(getResource().getString(R.string.recognize_ask_reminder_end_time), STEP_END_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (STEP_END_TIME.equals(id)) {
			try {
				mEndTime = DateTimeFormater.timeServerFormat.parse(data);
				// check if end time is less than start time, show error toast and set default end time is
				// start time + 1 hour
				if (DateTimeFormater.timeServerFormat.format(mEndTime).compareToIgnoreCase(
						DateTimeFormater.timeServerFormat.format(mStartTime)) < 0) {
					Calendar calendar = Calendar.getInstance();
					getOutput().showGuide(getResource().getString(R.string.msg_err_end_time_must_greater_start_time));
					calendar.setTime(mStartTime);
					calendar.add(Calendar.HOUR, 1);
					mEndTime = calendar.getTime();
				}
				Intent intent = new Intent(getContext(), ReminderAddActivity.class);
				intent.putExtra(Consts.PARAMS_EVENT_TITLE, mName);
				intent.putExtra(Consts.PARAMS_EVENT_CONTENT, mDesc);
				intent.putExtra(Consts.PARAMS_EVENT_START_TIME, mStartTime.getTime());
				intent.putExtra(Consts.PARAMS_EVENT_END_TIME, mEndTime.getTime());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void onFail(String data, String id) {
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
