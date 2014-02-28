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
import org.cnc.msrobot.activity.CalendarSampleActivity;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

/**
 * Asynchronous task that also takes care of common needs, such as displaying progress, authorization, exception
 * handling, and notifying UI when operation succeeded.
 * 
 * @author Yaniv Inbar
 */
public abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

	protected final CalendarEventActivity activity;
	protected final com.google.api.services.calendar.Calendar client;
	protected boolean bRefreshView = false;

	protected CalendarAsyncTask(CalendarEventActivity activity) {
		this.activity = activity;
		client = activity.client;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		activity.numAsyncTasks++;
	}

	@Override
	protected final Boolean doInBackground(Void... ignored) {
		try {
			doInBackground();
			return true;
		} catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
			activity.showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());
		} catch (UserRecoverableAuthIOException userRecoverableException) {
			activity.startActivityForResult(userRecoverableException.getIntent(),
					CalendarSampleActivity.REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			Utils.logAndShow(activity, CalendarSampleActivity.TAG, e);
		}
		return false;
	}

	@Override
	protected final void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		doInPostExecute(success);
		if (0 == --activity.numAsyncTasks) {
			// progressBar.setVisibility(View.GONE);
		}
		if (success && bRefreshView) {
			activity.refreshView();
		}
	}

	abstract protected void doInBackground() throws IOException;

	protected void doInPostExecute(Boolean success) {
	}
}
