package org.cnc.msrobot.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import org.cnc.msrobot.resource.EventResource;

import android.support.v4.app.FragmentManager;

public final class WeekPagerAdapter extends SchedulePagerAdapter {

	public WeekPagerAdapter(FragmentManager fm, ArrayList<EventResource> listEvent) {
		super(fm, listEvent);
		intervalDay = 7;
	}

	// Lazily create the fragments
	@Override
	public ArrayList<ScheduleFragment> getFragments() {
		if (fragments == null) {
			final Calendar calendar = Calendar.getInstance();
			fragments = new ArrayList<ScheduleFragment>();
			for (int i = 0; i < getCount(); i++) {
				WeekFragment fragment = new WeekFragment();
				fragments.add(fragment);
				if (i == 0) {
					// current week
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 1) {
					// next week
					calendar.add(Calendar.DATE, 7);
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 2) {
					// next two week
					calendar.add(Calendar.DATE, 14);
					fragment.setCurrentDate(calendar.getTime());
				} else if (i == 3) {
					// previous week
					calendar.add(Calendar.DATE, -21);
					fragment.setCurrentDate(calendar.getTime());
				}
				fragment.addEventList(listEvent);
			}
		}
		return fragments;
	}
}
