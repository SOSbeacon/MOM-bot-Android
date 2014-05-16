package org.cnc.mombot.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.cnc.mombot.resource.EventResource;

import android.lib.schedule.ScheduleView;
import android.support.v4.app.Fragment;

public abstract class ScheduleFragment extends Fragment {
	protected Date currentDate;
	protected ScheduleView<?> scheduleView;
	protected ArrayList<EventResource> listEvent;

	public void setCurrentDate(Date date) {
		this.currentDate = date;
	}

	public void addEventList(ArrayList<EventResource> listEvent) {
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
				EventResource e = listEvent.get(i);
				if (e.start() == e.end()) {
					calendar.setTime(e.start());
					calendar.set(Calendar.HOUR_OF_DAY, 23);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					e.setEnd(calendar.getTime());
				}
				scheduleView.addEventDate(e.start(), e.end());
			}
			scheduleView.refreshCalendar();
		}
		this.scheduleView.setOnPreviousClickListener(new ScheduleView.OnPreviousClickListener() {
			@Override
			public void onPreviousClick() {
				((CalendarEventFragment) ScheduleFragment.this.getParentFragment()).showPrevious();
			}
		});

		this.scheduleView.setOnNextClickListener(new ScheduleView.OnNextClickListener() {
			@Override
			public void onNextClick() {
				((CalendarEventFragment) ScheduleFragment.this.getParentFragment()).showNext();
			}
		});
	}
}
