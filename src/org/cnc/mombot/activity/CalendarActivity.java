package org.cnc.mombot.activity;

import org.cnc.mombot.R;
import org.cnc.mombot.fragment.CalendarEventFragment;
import org.cnc.mombot.utils.CustomActionBar;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class CalendarActivity extends BaseActivity implements OnClickListener {
	CalendarEventFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		// set custom action bar
		getCusomActionBar().setType(CustomActionBar.TYPE_CALENDAR);
		getCusomActionBar().setOnClickListener(this);
		initFragment(CalendarEventFragment.TYPE_MONTH);
	}

	private void initFragment(int type) {
		mFragment = new CalendarEventFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CalendarEventFragment.EXTRA_TYPE, type);
		mFragment.setArguments(bundle);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.llFragment, mFragment);
		fragmentTransaction.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvCalendarMonthly:
				initFragment(CalendarEventFragment.TYPE_MONTH);
				break;
			case R.id.tvCalendarWeekly:
				initFragment(CalendarEventFragment.TYPE_WEEK);
				break;
			case R.id.tvCalendarDaily:
				initFragment(CalendarEventFragment.TYPE_DAY);
				break;
		}
	}

}
