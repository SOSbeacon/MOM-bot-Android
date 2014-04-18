package android.lib.scheduleview.samples;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public final class ScheduleActivity extends FragmentActivity {
    public static final String EXTRA_TYPE = "TYPE"; //$NON-NLS-1$

    public static final int TYPE_MONTH = 0;
    public static final int TYPE_WEEK  = 1;
    public static final int TYPE_DAY   = 2;

    private ViewPager pager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_schedule);

        this.pager = (ViewPager)this.findViewById(R.id.pager);

        switch (this.getIntent().getIntExtra(ScheduleActivity.EXTRA_TYPE, ScheduleActivity.TYPE_MONTH)) {
            case TYPE_MONTH:
                this.pager.setAdapter(new MonthPagerAdapter(this.getSupportFragmentManager()));

                break;

            case TYPE_WEEK:
                this.pager.setAdapter(new WeekPagerAdapter(this.getSupportFragmentManager()));

                break;

            case TYPE_DAY:
                this.pager.setAdapter(new DayPagerAdapter(this.getSupportFragmentManager()));

                break;
        }

        this.pager.setOffscreenPageLimit(1);
        this.pager.setCurrentItem(1);
    }

    void showPrevious() {
        if (this.pager.getCurrentItem() > 0) {
            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1, true);
        }
    }

    void showNext() {
        if (this.pager.getCurrentItem() < 3) {
            this.pager.setCurrentItem(this.pager.getCurrentItem() + 1, true);
        }
    }
}
