/*
 * Copyright 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.cnc.msrobot.MainApplication;
import org.cnc.mombot.R;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.EventType;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DateTimeFormater;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * Activity to add or edit a calendar.
 * 
 * @author thanhle
 */
public class ReminderAddActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {
	private EditText tvTitle, tvDesc;
	private TextView tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvRepeatEvery, tvRepeatEndDate;
	private String id;
	private Date startTime, endTime, endDate;
	private Spinner spinnerRepeat, spinnerRepeatEvery;
	private int mRepeatEvery = 1;
	private int mRepeatType = 0;
	private CheckBox mChkRepeat;
	private View rlRepeat;

	Listener<EventResource[]> mRequestCreateEventListener = new Listener<EventResource[]>() {

		@Override
		public void onResponse(EventResource[] response) {
			if (response == null) {
				showCenterToast(R.string.msg_err_create_event);
			} else {
				showCenterToast(R.string.msg_info_create_event);
				// set reminder for alarm
				for (int i = 0; i < response.length; i++) {
					MainApplication.alarm.setReminder(getApplicationContext(), response[i]);
				}
			}
			finish();
		}
	};

	ErrorListener mRequestCreateEventError = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			showCenterToast(R.string.msg_err_create_event);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar_event_add_edit);
		tvTitle = (EditText) findViewById(R.id.tvSummary);
		tvDesc = (EditText) findViewById(R.id.tvDesc);
		tvStartDate = (TextView) findViewById(R.id.tvStartDate);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndDate = (TextView) findViewById(R.id.tvEndDate);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		rlRepeat = findViewById(R.id.rlRepeat);
		tvRepeatEndDate = (TextView) findViewById(R.id.tvRepeatEndDate);
		tvRepeatEvery = (TextView) findViewById(R.id.tvRepeatEvery);
		spinnerRepeat = (Spinner) findViewById(R.id.spRepeatType);
		spinnerRepeatEvery = (Spinner) findViewById(R.id.spRepeatEvery);
		mChkRepeat = (CheckBox) findViewById(R.id.chkRepeat);

		// get repeat type array and set adapter for spinner repeats
		final SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_repeat_type,
				android.R.layout.simple_spinner_dropdown_item);
		spinnerRepeat.setAdapter(mSpinnerAdapter);
		spinnerRepeat.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				mRepeatType = position;
				if (position == 0) {
					findViewById(R.id.tvRepeatOn).setVisibility(View.GONE);
					findViewById(R.id.llRepeatOn).setVisibility(View.GONE);
				} else {
					findViewById(R.id.tvRepeatOn).setVisibility(View.VISIBLE);
					findViewById(R.id.llRepeatOn).setVisibility(View.VISIBLE);
				}
				setRepeatEvery();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
			}
		});

		// build repeat every spinner
		final ArrayAdapter<Integer> mSpinnerRepeatEveryAdapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < 30; i++) {
			mSpinnerRepeatEveryAdapter.add(i + 1);
		}
		spinnerRepeatEvery.setAdapter(mSpinnerRepeatEveryAdapter);
		spinnerRepeatEvery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				mRepeatEvery = position + 1;
				setRepeatEvery();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
			}
		});

		// change action text in action bar and set listener
		getCusomActionBar().setActionText(getString(R.string.common_save));
		getCusomActionBar().setOnClickListener(this);
		getCusomActionBar().setType(CustomActionBar.TYPE_SEND);

		// set click listener for TextView
		tvStartDate.setOnClickListener(this);
		tvEndDate.setOnClickListener(this);
		tvStartTime.setOnClickListener(this);
		tvEndTime.setOnClickListener(this);
		tvRepeatEndDate.setOnClickListener(this);
		mChkRepeat.setOnCheckedChangeListener(this);

		Bundle bundle = getIntent().getExtras();
		// check bundle exists, set default value from bundle
		if (bundle != null) {
			id = bundle.getString("id");
			tvTitle.setText(bundle.getString(Consts.PARAMS_EVENT_TITLE));
			tvDesc.setText(bundle.getString(Consts.PARAMS_EVENT_CONTENT));
			// parse time has error, startTime and endTime maybe null, set default later
			try {
				if (bundle.containsKey(Consts.PARAMS_EVENT_START_TIME)) {
					startTime = new Date(bundle.getLong(Consts.PARAMS_EVENT_START_TIME));
				}
				if (bundle.containsKey(Consts.PARAMS_EVENT_END_TIME)) {
					endTime = new Date(bundle.getLong(Consts.PARAMS_EVENT_END_TIME));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// check if startTime and endTime null, set default startTime, endTime
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		// set start time default
		if (startTime == null) startTime = calendar.getTime();
		// set end time default
		// end time default is start time default + 1 hour
		calendar.add(Calendar.HOUR, 1);
		if (endTime == null) endTime = calendar.getTime();

		// set end date for repeat default
		// end date = start time
		calendar.setTime(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		endDate = calendar.getTime();

		tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
		tvStartTime.setText(DateTimeFormater.timeFormater.format(startTime));

		tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
		tvEndTime.setText(DateTimeFormater.timeFormater.format(endTime));
		tvRepeatEndDate.setText(DateTimeFormater.dateFullFormater.format(endDate));
	}

	private void setRepeatEvery() {
		switch (mRepeatType) {
			case 0: // daily
				tvRepeatEvery.setText(getResources().getQuantityString(R.plurals.day, mRepeatEvery));
				break;
			case 1: // Weekly
				tvRepeatEvery.setText(getResources().getQuantityString(R.plurals.week, mRepeatEvery));
				break;
			case 2: // Monthly
				tvRepeatEvery.setText(getResources().getQuantityString(R.plurals.month, mRepeatEvery));
				break;
			default:
				break;
		}
	}

	private void sendCreateReminderRequest() {
		try {
			String summary = tvTitle.getText().toString();
			String desc = tvDesc.getText().toString();
			if (summary.length() > 0) {
				Bundle bundle = new Bundle();
				if (id != null) {
					bundle.putString(Consts.PARAMS_ID, id);
				}
				bundle.putString(Consts.PARAMS_EVENT_TITLE, summary);
				bundle.putString(Consts.PARAMS_EVENT_CONTENT, desc);
				bundle.putString(Consts.PARAMS_EVENT_START_TIME, DateTimeFormater.timeServerFormat.format(startTime));
				bundle.putString(Consts.PARAMS_EVENT_END_TIME, DateTimeFormater.timeServerFormat.format(endTime));
				if (rlRepeat.getVisibility() == View.VISIBLE) {
					ArrayList<String> repeatOn = new ArrayList<String>();
					// repeat type
					switch (mRepeatType) {
						case 0:
							bundle.putString(Consts.PARAMS_EVENT_TYPE, EventType.TYPE_DAILY);
							break;
						case 1:
							bundle.putString(Consts.PARAMS_EVENT_TYPE, EventType.TYPE_WEEKLY);
							break;
						case 2:
							bundle.putString(Consts.PARAMS_EVENT_TYPE, EventType.TYPE_MONTHLY);
							break;
						default:
							break;
					}
					if (mRepeatType != 0) {
						// weekly or monthly, get repeat on
						LinearLayout llRepeatOn = (LinearLayout) findViewById(R.id.llRepeatOn);
						for (int i = 0; i < llRepeatOn.getChildCount(); i++) {
							CheckBox chk = (CheckBox) llRepeatOn.getChildAt(i);
							if (chk.isChecked()) {
								repeatOn.add(i + "");
							}
						}
						if (repeatOn.size() > 0) {
							bundle.putStringArrayList(Consts.PARAMS_EVENT_DAY_WEEK, repeatOn);
						}
					}
					// repeat interval
					bundle.putString(Consts.PARAMS_EVENT_REPEAT, mRepeatEvery + "");
					bundle.putString(Consts.PARAMS_EVENT_END_DATE, DateTimeFormater.timeServerFormat.format(endDate));
				} else {
					bundle.putString(Consts.PARAMS_EVENT_TYPE, EventType.TYPE_NOREPEAT);
				}
				mRequestManager.request(Actions.ACTION_CREATE_EVENT, bundle, mRequestCreateEventListener,
						mRequestCreateEventError);
			} else {
				finish();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			showCenterToast(R.string.msg_err_create_event);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tvAction) {
			sendCreateReminderRequest();
		} else {
			final Calendar calendar = Calendar.getInstance();
			switch (v.getId()) {
				case R.id.tvStartDate:
				case R.id.tvStartTime:
					calendar.setTime(startTime);
					break;
				case R.id.tvEndDate:
				case R.id.tvEndTime:
					calendar.setTime(endTime);
					break;
				case R.id.tvRepeatEndDate:
					calendar.setTime(endDate);
					break;
			}
			int date = calendar.get(Calendar.DATE);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			switch (v.getId()) {
				case R.id.tvRepeatEndDate: {
					DatePickerDialog dialogEndDate = new DatePickerDialog(this, new OnDateSetListener() {
						boolean fired = false;

						@Override
						public void onDateSet(DatePicker view, int year, int month, int day) {
							// onDateSet call twice, so, we will check for first call
							if (!fired) {
								calendar.set(Calendar.DATE, day);
								calendar.set(Calendar.MONTH, month);
								calendar.set(Calendar.YEAR, year);
								endDate = calendar.getTime();
								// check if end time is less than start time, set default end time is
								// start time
								if (DateTimeFormater.timeServerFormat.format(endDate).compareToIgnoreCase(
										DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
									showCenterToast(R.string.msg_err_end_time_must_greater_start_time);
									calendar.setTime(startTime);
									calendar.set(Calendar.HOUR_OF_DAY, 23);
									calendar.set(Calendar.MINUTE, 59);
									calendar.set(Calendar.SECOND, 59);
									endDate = calendar.getTime();
								}
								tvRepeatEndDate.setText(DateTimeFormater.dateFullFormater.format(endDate));
								fired = true;
							}
						}
					}, year, month, date);
					dialogEndDate.setTitle("Select End Date repeat");
					dialogEndDate.show();
					break;
				}
				case R.id.tvStartDate: {
					DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {
						boolean fired = false;

						@Override
						public void onDateSet(DatePicker view, int year, int month, int day) {
							// onDateSet call twice, so, we will check for first call
							if (!fired) {
								calendar.set(Calendar.DATE, day);
								calendar.set(Calendar.MONTH, month);
								calendar.set(Calendar.YEAR, year);
								startTime = calendar.getTime();
								tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
								// check if end time is less than start time, set default end time is
								// start time + 1 hour
								if (DateTimeFormater.timeServerFormat.format(endTime).compareToIgnoreCase(
										DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
									calendar.setTime(startTime);
									calendar.add(Calendar.HOUR, 1);
									endTime = calendar.getTime();
									tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
									tvEndTime.setText(DateTimeFormater.timeFormater.format(endTime));
								}
								if (DateTimeFormater.timeServerFormat.format(endDate).compareToIgnoreCase(
										DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
									// set end date
									calendar.setTime(startTime);
									calendar.set(Calendar.HOUR_OF_DAY, 23);
									calendar.set(Calendar.MINUTE, 59);
									calendar.set(Calendar.SECOND, 59);
									endDate = calendar.getTime();
									tvRepeatEndDate.setText(DateTimeFormater.dateFullFormater.format(endDate));
								}
								fired = true;
							}
						}
					}, year, month, date);
					dialog.setTitle("Select Start Date");
					dialog.show();
					break;
				}
				case R.id.tvEndDate: {
					DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {
						boolean fired = false;

						@Override
						public void onDateSet(DatePicker view, int year, int month, int day) {
							// onDateSet call twice, so, we will check for first call
							if (!fired) {
								calendar.set(Calendar.DATE, day);
								calendar.set(Calendar.MONTH, month);
								calendar.set(Calendar.YEAR, year);
								endTime = calendar.getTime();
								// check if end time is less than start time, show error toast and set default end time
								// is
								// start time + 1 hour
								if (DateTimeFormater.timeServerFormat.format(endTime).compareToIgnoreCase(
										DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
									showCenterToast(R.string.msg_err_end_time_must_greater_start_time);
									calendar.setTime(startTime);
									calendar.add(Calendar.HOUR, 1);
									endTime = calendar.getTime();
								}
								tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
								fired = true;
							}
						}
					}, year, month, date);
					dialog.setTitle("Select End Date");
					dialog.show();
					break;
				}
				case R.id.tvStartTime: {
					TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
							calendar.set(Calendar.MINUTE, minute);
							startTime = calendar.getTime();
							tvStartTime.setText(DateTimeFormater.timeFormater.format(startTime));
							// check if end time is less than start time, set default end time is
							// start time + 1 hour
							if (DateTimeFormater.timeServerFormat.format(endTime).compareToIgnoreCase(
									DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
								// set end time
								calendar.setTime(startTime);
								calendar.add(Calendar.HOUR, 1);
								endTime = calendar.getTime();
								tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
								tvEndTime.setText(DateTimeFormater.timeFormater.format(endTime));
							}
							if (DateTimeFormater.timeServerFormat.format(endDate).compareToIgnoreCase(
									DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
								// set end date
								calendar.setTime(startTime);
								calendar.set(Calendar.HOUR_OF_DAY, 23);
								calendar.set(Calendar.MINUTE, 59);
								calendar.set(Calendar.SECOND, 59);
								endDate = calendar.getTime();
								tvRepeatEndDate.setText(DateTimeFormater.dateFullFormater.format(endDate));
							}
						}
					}, hour, minute, true);
					dialog.setTitle("Select Start Time");
					dialog.show();
					break;
				}
				case R.id.tvEndTime: {
					TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
							calendar.set(Calendar.MINUTE, minute);
							endTime = calendar.getTime();
							// check if end time is less than start time, show error toast and set default end time is
							// start time + 1 hour
							if (DateTimeFormater.timeServerFormat.format(endTime).compareToIgnoreCase(
									DateTimeFormater.timeServerFormat.format(startTime)) < 0) {
								showCenterToast(R.string.msg_err_end_time_must_greater_start_time);
								calendar.setTime(startTime);
								calendar.add(Calendar.HOUR, 1);
								endTime = calendar.getTime();
							}
							tvEndTime.setText(DateTimeFormater.timeFormater.format(endTime));
						}
					}, hour, minute, true);
					dialog.setTitle("Select End Time");
					dialog.show();
					break;
				}
				default:
					break;
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			rlRepeat.setVisibility(View.VISIBLE);
		} else {
			rlRepeat.setVisibility(View.GONE);
		}
	}
}
