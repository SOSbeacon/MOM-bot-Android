package android.lib.scheduleview.samples;

import android.lib.schedule.MonthView;
import android.lib.schedule.MonthView.MonthAdapter;
import android.view.View;
import android.view.ViewGroup;

final class SimpleMonthAdapter extends MonthAdapter {
    public SimpleMonthAdapter(final MonthView monthView) {
        monthView.super();
    }

    @Override
    public Object getItem(final int position) {
        return Integer.valueOf(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        return convertView;
    }
}
