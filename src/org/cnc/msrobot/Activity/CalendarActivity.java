package org.cnc.msrobot.activity;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.CalendarEventFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class CalendarActivity extends BaseActivity {
	CalendarEventFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		mFragment = new CalendarEventFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_MONTH);
		mFragment.setArguments(bundle);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.llFragment, mFragment);
		fragmentTransaction.commit();
	}
}
