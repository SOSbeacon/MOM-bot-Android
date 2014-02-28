package org.cnc.msrobot.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.cnc.msrobot.activity.CalendarEventActivity;

import android.lib.schedule.ScheduleView;
import android.support.v4.app.Fragment;

import com.google.api.services.calendar.model.Event;

public abstract class ScheduleFragment extends Fragment {
	protected Date currentDate;
	protected ScheduleView<?> scheduleView;
	protected ArrayList<Event> listEvent;

	public void setCurrentDate(Date date) {
		this.currentDate = date;
	}

	public void addEventList(ArrayList<Event> listEvent) {
		this.listEvent = listEvent;
	}

	public void refreshView() {
		if (scheduleView == null) return;
		if (currentDate != null) {
			this.scheduleView.setCurrentDate(currentDate);
		}
		if (listEvent != null) {
			int size = listEvent.size();
			final Calendar calendar = Calendar.getInstance();
			scheduleView.cleanEventData();
			for (int i = 0; i < size; i++) {
				Event e = listEvent.get(i);
				Date start, end;
				if (e.getStart().getDateTime() != null) {
					start = new Date(e.getStart().getDateTime().getValue());
				} else {
					calendar.setTimeInMillis(e.getStart().getDate().getValue());
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					start = calendar.getTime();
				}
				if (e.getEnd().getDateTime() != null) {
					end = new Date(e.getEnd().getDateTime().getValue());
				} else {
					calendar.setTimeInMillis(e.getEnd().getDate().getValue());
					calendar.set(Calendar.HOUR_OF_DAY, 23);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					end = calendar.getTime();
				}
				scheduleView.addEventDate(start, end);
			}
			scheduleView.refreshCalendar();
		}
		this.scheduleView.setOnPreviousClickListener(new ScheduleView.OnPreviousClickListener() {
			@Override
			public void onPreviousClick() {
				((CalendarEventActivity) ScheduleFragment.this.getActivity()).showPrevious();
			}
		});

		this.scheduleView.setOnNextClickListener(new ScheduleView.OnNextClickListener() {
			@Override
			public void onNextClick() {
				((CalendarEventActivity) ScheduleFragment.this.getActivity()).showNext();
			}
		});
	}
}
