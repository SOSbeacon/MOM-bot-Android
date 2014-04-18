package android.lib.scheduleview.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public final class MainActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        this.findViewById(R.id.month_view_button).setOnClickListener(this);
        this.findViewById(R.id.week_view_button).setOnClickListener(this);
        this.findViewById(R.id.day_view_button).setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        final Intent intent = new Intent(this, ScheduleActivity.class);

        switch (view.getId()) {
            case R.id.month_view_button:
                intent.putExtra(ScheduleActivity.EXTRA_TYPE, ScheduleActivity.TYPE_MONTH);

                break;

            case R.id.week_view_button:
                intent.putExtra(ScheduleActivity.EXTRA_TYPE, ScheduleActivity.TYPE_WEEK);

                break;

            case R.id.day_view_button:
                intent.putExtra(ScheduleActivity.EXTRA_TYPE, ScheduleActivity.TYPE_DAY);

                break;
        }

        this.startActivity(intent);
    }
}
