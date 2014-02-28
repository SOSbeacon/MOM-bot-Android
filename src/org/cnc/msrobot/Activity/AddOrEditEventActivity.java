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

import com.google.api.client.util.DateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Activity to add or edit a calendar.
 * 
 * @author Yaniv Inbar
 */
public class AddOrEditEventActivity extends Activity {

	private EditText summaryEditText;
	private TimePicker tprStart, tprEnd;
	private String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar_event_add_edit);
		summaryEditText = (EditText) findViewById(R.id.summaryText);
		tprStart = (TimePicker) findViewById(R.id.tprStart);
		tprEnd = (TimePicker) findViewById(R.id.tprEnd);
		TextView titleTextView = (TextView) findViewById(R.id.textViewTitle);
		id = getIntent().getStringExtra("id");

		if (id != null) {
			titleTextView.setText(R.string.edit_event);
			summaryEditText.setText(getIntent().getStringExtra("summary"));
		} else {
			titleTextView.setText(R.string.event_add);
		}
	}

	public void onSave(View view) {
		String summary = summaryEditText.getText().toString();
		if (summary.length() > 0) {
			Intent t = new Intent();
			if (id != null) {
				t.putExtra("id", id);
			}
			t.putExtra("summary", summary);

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, tprStart.getCurrentHour());
			calendar.set(Calendar.MINUTE, tprStart.getCurrentMinute());
			calendar.set(Calendar.SECOND, 0);
			Date start = calendar.getTime();
			calendar.set(Calendar.HOUR_OF_DAY, tprEnd.getCurrentHour());
			calendar.set(Calendar.MINUTE, tprEnd.getCurrentMinute());
			Date end = calendar.getTime();
			t.putExtra("start", new DateTime(start).getValue());
			t.putExtra("end", new DateTime(end).getValue());
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
}
