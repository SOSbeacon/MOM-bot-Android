package org.cnc.msrobot.fragment;

import org.cnc.msrobot.R;

import android.lib.schedule.DayView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class DayFragment extends ScheduleFragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.scheduleView = (DayView) inflater.inflate(R.layout.fragment_day, null);
		refreshView();
		return this.scheduleView;
	}
}
