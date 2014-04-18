package android.lib.scheduleview.samples;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

final class MonthPagerAdapter extends FragmentStatePagerAdapter {
    public MonthPagerAdapter(final FragmentManager manager) {
        super(manager);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(final int position) {
        final Calendar calendar = Calendar.getInstance();

        if (position != this.getCount() / 2) {
            calendar.add(Calendar.MONTH, position - 1);
        }

        final Bundle bundle = new Bundle();
        bundle.putInt(ScheduleActivity.EXTRA_TYPE, ScheduleActivity.TYPE_MONTH);
        bundle.putLong(MonthFragment.EXTRA_DATE, calendar.getTimeInMillis());

        final Fragment fragment = new MonthFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
