package org.cnc.msrobot.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import org.cnc.msrobot.resource.EventResource;

import android.support.v4.app.FragmentManager;

public final class DayPagerAdapter extends SchedulePagerAdapter {

	public DayPagerAdapter(FragmentManager fm, ArrayList<EventResource> listEvent) {
		super(fm, listEvent);
		intervalDay = 1;
	}

	// Lazily create the fragments
	@Override
	public ArrayList<ScheduleFragment> getFragments() {
		if (fragments == null) {
			final Calendar calendar = Calendar.getInstance();
			fragments = new ArrayList<ScheduleFragment>();
			for (int i = 0; i < getCount(); i++) {
				DayFragment fragment = new DayFragment();
				fragments.add(fragment);
				if (i == 0) {
					// current day
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 1) {
					// next date
					calendar.add(Calendar.DATE, 1);
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 2) {
					// next two day
					calendar.add(Calendar.DATE, 1);
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 3) {
					// previous day
					calendar.add(Calendar.DATE, -3);
					fragment.setCurrentDate(calendar.getTime());
				}
				fragment.addEventList(listEvent);
			}
		}
		return fragments;
	}
}
