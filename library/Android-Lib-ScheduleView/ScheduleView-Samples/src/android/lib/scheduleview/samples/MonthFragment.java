package android.lib.scheduleview.samples;

import java.util.Date;

import android.lib.schedule.MonthView;
import android.lib.schedule.ScheduleView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class MonthFragment extends Fragment {
    public static final String EXTRA_DATE = "DATE"; //$NON-NLS-1$

    private MonthView monthView;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.monthView.setCurrentDate(new Date(this.getArguments() == null ? savedInstanceState.getLong(MonthFragment.EXTRA_DATE) : this.getArguments().getLong(MonthFragment.EXTRA_DATE)));
        this.monthView.setAdapter(new SimpleMonthAdapter(this.monthView));

        this.monthView.setOnPreviousClickListener(new ScheduleView.OnPreviousClickListener() {
            @Override
            public void onPreviousClick() {
                ((ScheduleActivity)MonthFragment.this.getActivity()).showPrevious();
            }
        });

        this.monthView.setOnNextClickListener(new ScheduleView.OnNextClickListener() {
            @Override
            public void onNextClick() {
                ((ScheduleActivity)MonthFragment.this.getActivity()).showNext();
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final int type = this.getArguments() == null ? savedInstanceState.getInt(ScheduleActivity.EXTRA_TYPE) : this.getArguments().getInt(ScheduleActivity.EXTRA_TYPE);

        switch (type) {
            case ScheduleActivity.TYPE_MONTH:
                this.monthView = (MonthView)inflater.inflate(R.layout.fragment_month, null);

                break;
        }

        return this.monthView;
    }
}
