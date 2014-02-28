/*
 * Copyright (c) 2012 Google Inc.
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

package org.cnc.msrobot.task.calendar;

import java.io.IOException;

import org.cnc.msrobot.activity.CalendarEventActivity;

import android.text.TextUtils;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

/**
 * Asynchronously load the calendars.
 * 
 * @author Yaniv Inbar
 */
public class AsyncLoadCalendars extends CalendarAsyncTask {
	public static final String CALENDAR_NAME = "msrobot";

	AsyncLoadCalendars(CalendarEventActivity calendarSample) {
		super(calendarSample);
	}

	@Override
	protected void doInBackground() throws IOException {
		CalendarList feed = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
		for (CalendarListEntry calendar : feed.getItems()) {
			if (CALENDAR_NAME.equals(calendar.getSummary())) {
				activity.calendarId = calendar.getId();
			}
		}
	}

	@Override
	protected void doInPostExecute(Boolean success) {
		// check if msrobot calendar exists, if not, insert
		if (TextUtils.isEmpty(activity.calendarId)) {
			Calendar calendar = new Calendar();
			calendar.setSummary(CALENDAR_NAME);
			new AsyncInsertCalendar(activity, calendar);
		} else {
			AsyncLoadEvents.run(activity);
		}
	}

	public static void run(CalendarEventActivity calendarSample) {
		new AsyncLoadCalendars(calendarSample).execute();
	}
}
