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
import java.util.List;

import org.cnc.msrobot.activity.CalendarEventActivity;

import android.text.TextUtils;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Asynchronously load the event.
 * 
 * @author ThanhLCM
 */
public class AsyncLoadEvents extends CalendarAsyncTask {
	AsyncLoadEvents(CalendarEventActivity calendarSample) {
		super(calendarSample);
	}

	@Override
	protected void doInBackground() throws IOException {
		if (TextUtils.isEmpty(activity.calendarId)) { return; }
		String pageToken = null;
		activity.listEvent.clear();
		do {
			Events events = client.events().list(activity.calendarId).setPageToken(pageToken).execute();
			List<Event> items = events.getItems();
			for (Event event : items) {
				activity.listEvent.add(event);
			}
			pageToken = events.getNextPageToken();
		} while (pageToken != null);
	}

	@Override
	protected void doInPostExecute(Boolean success) {
		bRefreshView = true;
	}

	public static void run(CalendarEventActivity calendarSample) {
		new AsyncLoadEvents(calendarSample).execute();
	}
}
