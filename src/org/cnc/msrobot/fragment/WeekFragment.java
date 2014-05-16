package org.cnc.msrobot.fragment;

import org.cnc.mombot.R;

import android.lib.schedule.WeekView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class WeekFragment extends ScheduleFragment {
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.scheduleView = (WeekView) inflater.inflate(R.layout.fragment_week, null);
		refreshView();
		return this.scheduleView;
	}
}
