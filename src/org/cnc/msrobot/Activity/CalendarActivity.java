package org.cnc.msrobot.activity;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.CalendarEventFragment;

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

		initFragment(CalendarEventFragment.TYPE_MONTH);

		findViewById(R.id.btnCalendarMonth).setOnClickListener(this);
		findViewById(R.id.btnCalendarWeek).setOnClickListener(this);
		findViewById(R.id.btnCalendarDay).setOnClickListener(this);
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
			case R.id.btnCalendarMonth:
				initFragment(CalendarEventFragment.TYPE_MONTH);
				break;
			case R.id.btnCalendarWeek:
				initFragment(CalendarEventFragment.TYPE_WEEK);
				break;
			case R.id.btnCalendarDay:
				initFragment(CalendarEventFragment.TYPE_DAY);
				break;
		}
	}

}
