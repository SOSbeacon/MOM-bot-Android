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

import java.util.Calendar;
import java.util.Date;

import org.cnc.msrobot.R;
import org.cnc.msrobot.utils.DateTimeFormater;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Activity to add or edit a calendar.
 * 
 * @author Yaniv Inbar
 */
public class AddOrEditEventActivity extends Activity implements OnClickListener {
	public static final String EXTRA_ID = "EXTRA_ID";
	public static final String EXTRA_SUMMARY = "EXTRA_SUMMARY";
	public static final String EXTRA_DESC = "EXTRA_DESC";
	public static final String EXTRA_START_TIME = "EXTRA_START_TIME";
	public static final String EXTRA_END_TIME = "EXTRA_END_TIME";
	private EditText summaryEditText, tvDesc;
	private TextView tvStartDate, tvStartTime, tvEndDate, tvEndTime;
	private String id;
	private Date startTime, endTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar_event_add_edit);
		summaryEditText = (EditText) findViewById(R.id.tvSummary);
		tvDesc = (EditText) findViewById(R.id.tvDesc);
		tvStartDate = (TextView) findViewById(R.id.tvStartDate);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndDate = (TextView) findViewById(R.id.tvEndDate);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		TextView titleTextView = (TextView) findViewById(R.id.textViewTitle);
		id = getIntent().getStringExtra("id");

		tvStartDate.setOnClickListener(this);
		tvEndDate.setOnClickListener(this);
		tvStartTime.setOnClickListener(this);
		tvEndTime.setOnClickListener(this);

		if (id != null) {
			titleTextView.setText(R.string.edit_event);
			summaryEditText.setText(getIntent().getStringExtra("summary"));
		} else {
			titleTextView.setText(R.string.event_add);
		}

		Calendar calendar = Calendar.getInstance();
		startTime = calendar.getTime();
		tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
		tvStartTime.setText(DateTimeFormater.timeFormater.format(startTime));

		calendar.add(Calendar.HOUR, 1);
		endTime = calendar.getTime();
		tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
		tvEndTime.setText(DateTimeFormater.timeFormater.format(endTime));
	}

	public void onSave(View view) {
		String summary = summaryEditText.getText().toString();
		String desc = tvDesc.getText().toString();
		if (summary.length() > 0) {
			Intent t = new Intent();
			if (id != null) {
				t.putExtra(EXTRA_ID, id);
			}
			t.putExtra(EXTRA_SUMMARY, summary);
			t.putExtra(EXTRA_DESC, desc);
			t.putExtra(EXTRA_START_TIME, startTime.getTime());
			t.putExtra(EXTRA_END_TIME, startTime.getTime());
			setResult(Activity.RESULT_OK, t);
		} else {
			setResult(Activity.RESULT_CANCELED);
		}
		finish();
	}

	public void onCancel(View view) {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void onClick(View v) {
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
		}
		int date = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		switch (v.getId()) {
			case R.id.tvStartDate: {
				DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						calendar.set(Calendar.DATE, day);
						calendar.set(Calendar.MONTH, month);
						calendar.set(Calendar.YEAR, year);
						startTime = calendar.getTime();
						tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
					}
				}, year, month, date);
				dialog.setTitle("Select Start Date");
				dialog.show();
				break;
			}
			case R.id.tvEndDate: {
				DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						calendar.set(Calendar.DATE, day);
						calendar.set(Calendar.MONTH, month);
						calendar.set(Calendar.YEAR, year);
						endTime = calendar.getTime();
						tvEndDate.setText(DateTimeFormater.dateFullFormater.format(endTime));
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
