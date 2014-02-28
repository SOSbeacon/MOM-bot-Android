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

import com.google.api.services.calendar.model.Event;

/**
 * Asynchronously insert a new event.
 * 
 * @author ThanhLCM
 */
public class AsyncInsertEvent extends CalendarAsyncTask {

	private final Event entry;

	public AsyncInsertEvent(CalendarEventActivity calendarSample, Event entry) {
		super(calendarSample);
		this.entry = entry;
	}

	@Override
	protected void doInBackground() throws IOException {
		Event createdEvent = client.events().insert(activity.calendarId, entry).execute();
	}
}
