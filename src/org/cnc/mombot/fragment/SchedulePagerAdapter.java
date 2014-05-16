package org.cnc.mombot.fragment;

import java.util.ArrayList;

import org.cnc.mombot.resource.EventResource;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public abstract class SchedulePagerAdapter extends FragmentStatePagerAdapter {

	protected ArrayList<ScheduleFragment> fragments;
	protected int intervalDay = 1;

	public abstract ArrayList<ScheduleFragment> getFragments();

	protected ArrayList<EventResource> listEvent;

	public int getInterval() {
		return intervalDay;
	}

	public SchedulePagerAdapter(FragmentManager fm, ArrayList<EventResource> listEvent) {
		super(fm);
		this.listEvent = listEvent;
	}

	@Override
	public Fragment getItem(int position) {
		ScheduleFragment fragment = getFragments().get(position);
		fragment.addEventList(listEvent);
		return fragment;
	}

	@Override
	public int getCount() {
		// We need 4 gridviews for previous month, current month and next month,
		// and 1 extra fragment for fragment recycle
		return CalendarEventFragment.NUMBER_OF_PAGES;
	}

	public void setListEvent(ArrayList<EventResource> listEvent) {
		for (int i = 0; i < getCount(); i++) {
			ScheduleFragment fragment = getFragments().get(i);
			fragment.addEventList(listEvent);
			fragment.refreshView();
		}
	}
}
