package android.lib.scheduleview.samples;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

final class DayPagerAdapter extends FragmentStatePagerAdapter {
    public DayPagerAdapter(final FragmentManager manager) {
        super(manager);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(final int position) {
        return null;
    }
}
