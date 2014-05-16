package org.cnc.mombot.inputoutput;

import java.util.Calendar;
import java.util.Date;

import org.cnc.mombot.R;
import org.cnc.mombot.utils.DateTimeFormater;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class DateTimeInput implements Input, android.view.View.OnClickListener {
	private InputReceiveCallback callback;
	private Context mContext;
	private Date startTime;
	private Handler handler = new Handler();
	private TextView tvStartDate, tvStartTime;

	public DateTimeInput(Context context) {
		this.mContext = context;
	}

	@Override
	public void show(final String id) {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		final View viewChooseTime = layoutInflater.inflate(R.layout.layout_select_date_time, null);
		tvStartDate = (TextView) viewChooseTime.findViewById(R.id.tvStartDate);
		tvStartTime = (TextView) viewChooseTime.findViewById(R.id.tvStartTime);

		tvStartDate.setOnClickListener(this);
		tvStartTime.setOnClickListener(this);

		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.dialog_choose_time_title);

					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.SECOND, 0);
					calendar.add(Calendar.MINUTE, 1);
					startTime = calendar.getTime();
					tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
					tvStartTime.setText(DateTimeFormater.timeFormater.format(startTime));

					builder.setView(viewChooseTime);
					builder.setPositiveButton("Ok", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (callback != null) {
								callback.onReceive(DateTimeFormater.timeServerFormat.format(startTime), id);
							}
						}
					});
					builder.setNegativeButton("Cancel", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (callback != null) {
								callback.onReceive(null, id);
							}
						}
					});

					AlertDialog dialog = builder.create();
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

	}

	@Override
	public void setReceiveCallback(InputReceiveCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onClick(View v) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTime);
		int date = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		switch (v.getId()) {
			case R.id.tvStartDate: {
				DatePickerDialog dialog = new DatePickerDialog(mContext, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						calendar.set(Calendar.DATE, day);
						calendar.set(Calendar.MONTH, month);
						calendar.set(Calendar.YEAR, year);
						startTime = calendar.getTime();
						tvStartDate.setText(DateTimeFormater.dateFullFormater.format(startTime));
					}
				}, year, month, date);
				dialog.setTitle("Select Date");
				dialog.show();
				break;
			}
			case R.id.tvStartTime: {
				TimePickerDialog dialog = new TimePickerDialog(mContext, new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						startTime = calendar.getTime();
						tvStartTime.setText(DateTimeFormater.timeFormater.format(startTime));
					}
				}, hour, minute, true);
				dialog.setTitle("Select Time");
				dialog.show();
				break;
			}
		}
	}

}