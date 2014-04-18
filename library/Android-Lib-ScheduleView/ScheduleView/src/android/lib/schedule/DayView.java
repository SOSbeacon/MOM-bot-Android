package android.lib.schedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.lib.scheduleview.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DayView extends ScheduleView<DayView.DayAdapter> {
	private static final int INTERVAL = 60;
	private static final int START_INTERVAL = 0;
	private static final int END_INTERVAL = 24;
	private static final int ROWS = DayView.END_INTERVAL - DayView.START_INTERVAL;
	private static final int EVENT_COUNT = 10;

	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$

	private GridView gridView;
	private GridAdapter gridAdapter;
	private DayView.DayAdapter dayAdapter;

	private Date currentDate;

	private Date[] dayDates = new Date[DayView.ROWS];
	private String[] dayTimes = new String[DayView.ROWS];
	private boolean[] isPastDays = new boolean[DayView.ROWS];
	private int[][] dayEvents = new int[ROWS][EVENT_COUNT];
	private int eventCount = 0;
	private Calendar calendar = Calendar.getInstance();
	private int interval = DayView.INTERVAL;
	private int startInterval;
	private int endInterval;

	public DayView(final Context context) {
		super(context);
	}

	public DayView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public DayView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public final DayView.DayAdapter getAdapter() {
		return this.dayAdapter;
	}

	public final void setAdapter(final DayView.DayAdapter dayAdapter) {
		this.dayAdapter = dayAdapter;

		this.gridView.setAdapter(this.gridAdapter);

		this.requestLayout();
	}

	public final int getInterval() {
		return this.interval;
	}

	public final int getStartInterval() {
		return this.startInterval;
	}

	public final int getEndInterval() {
		return this.endInterval;
	}

	@Override
	public final Date getCurrentDate() {
		return this.currentDate;
	}

	@Override
	public final void setCurrentDate(final Date currentDate) {
		this.currentDate = currentDate;

		this.calendar.setTime(currentDate);

		this.calendar.set(Calendar.HOUR_OF_DAY, 0);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		this.calendar.set(Calendar.MILLISECOND, 0);

		this.setTitle(DateFormat.getDateInstance().format(currentDate));

		final Date now = new Date();

		this.calendar.add(Calendar.MINUTE, this.startInterval * this.interval);

		for (int i = 0; i < DayView.ROWS; i++) {
			this.dayTimes[i] = DayView.TIME_FORMAT.format(this.calendar.getTime());
			this.dayDates[i] = new Date(this.calendar.getTimeInMillis());
			this.isPastDays[i] = this.dayDates[i].before(now);

			this.calendar.add(Calendar.MINUTE, this.interval);
		}
	}

	@Override
	public void refreshCalendar() {
		if (this.gridView != null && this.gridAdapter != null) {
			this.gridAdapter.notifyDataSetChanged();
			this.gridView.invalidateViews();
		}
	}

	@Override
	public void cleanEventData() {
		dayEvents = new int[ROWS][EVENT_COUNT];
	}

	@Override
	public final void addEventDate(Date start, Date end) {
		int day = start.getDate();
		int month = start.getMonth();
		int year = start.getYear();
		if (currentDate.getDate() == day && currentDate.getMonth() == month && currentDate.getYear() == year) {
			// thuat toan them su kien vao mang
			final int startHour = start.getHours();
			final int endHour = end.getHours();
			int col = 0;
			if (eventCount > 0) {
				// quet tung dong
				for (int i = startHour; i <= endHour; i++) {
					// quet tung cot truoc do tren 1 dong, neu o co gia tri lon hon col thi o do da co event, tang col
					while (col < EVENT_COUNT && col < eventCount && dayEvents[i][col] > 0) {
						col++;
					}
				}
			}
			if (col >= EVENT_COUNT) return;
			for (int i = startHour; i <= endHour; i++) {
				fillEventDays(i, col, col + 1);
				if (i == startHour) {
					// so dau tien cua event la so am nguoc gia tri
					dayEvents[i][col] = -(col + 1);
				}
			}
			eventCount++;
		}
	}

	/**
	 * Ham de quy loan fill nhung o lien quan voi nhau
	 * 
	 * @param x
	 * @param y
	 */
	private void fillEventDays(int x, int y, int value) {
		if (dayEvents[x][y] < 0) {
			dayEvents[x][y] = -value;
		} else {
			dayEvents[x][y] = value;
		}
		if (x > 0 && dayEvents[x - 1][y] != 0 && Math.abs(dayEvents[x - 1][y]) < value) {
			fillEventDays(x - 1, y, value);
		}

		if (x < ROWS - 1 && dayEvents[x + 1][y] != 0 && Math.abs(dayEvents[x + 1][y]) < value) {
			fillEventDays(x + 1, y, value);
		}

		if (y > 0 && dayEvents[x][y - 1] != 0 && Math.abs(dayEvents[x][y - 1]) < value) {
			fillEventDays(x, y - 1, value);
		}

		if (y < EVENT_COUNT - 1 && dayEvents[x][y + 1] != 0 && Math.abs(dayEvents[x][y + 1]) < value) {
			fillEventDays(x, y + 1, value);
		}
	}

	@Override
	void initialize(final TypedArray array) {
		try {
			this.interval = array.getInt(R.styleable.ScheduleView_interval, DayView.INTERVAL);
			this.startInterval = array.getInt(R.styleable.ScheduleView_startInterval, DayView.START_INTERVAL);
			this.endInterval = array.getInt(R.styleable.ScheduleView_endInterval, DayView.END_INTERVAL);

			if (this.calendar == null) {
				this.dayDates = new Date[this.endInterval - this.startInterval];
				this.dayTimes = new String[this.endInterval - this.startInterval];
				this.isPastDays = new boolean[this.endInterval - this.startInterval];
				this.calendar = Calendar.getInstance();
			}

			final View root = View.inflate(this.getContext(), R.layout.day, this);

			super.initialize(array);

			// Day view
			this.gridView = (GridView) root.findViewById(R.id.day);
			this.gridAdapter = new GridAdapter();
			this.gridView.setAdapter(this.gridAdapter);

			this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
					if (DayView.this.getOnItemClickListener() != null) {
						DayView.this.getOnItemClickListener().onItemClick(null, view, position,
								DayView.this.getAdapter().getItemId(position));
					}
				}
			});

			this.gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position,
						final long id) {
					return DayView.this.getOnItemLongClickListener() == null ? false : DayView.this
							.getOnItemLongClickListener().onItemLongClick(null, view, position,
									DayView.this.getAdapter().getItemId(position));
				}
			});
		} catch (final Exception e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
		}
	}

	public abstract class DayAdapter extends BaseAdapter {
		protected DayAdapter() {
		}

		@Override
		public final int getCount() {
			return DayView.this.endInterval - DayView.this.startInterval;
		}

		public final Date getDate(final int position) {
			return DayView.this.dayDates[position];
		}

		public final boolean isPastDate(final int position) {
			return DayView.this.isPastDays[position];
		}
	}

	private final class GridAdapter extends BaseAdapter {
		final int fullWidth;
		final int padding;

		public GridAdapter() {
			int timeSize = getResources().getDimensionPixelSize(R.dimen.time_width);
			padding = getResources().getDimensionPixelSize(R.dimen.widget_spacing);
			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			fullWidth = screenWidth - timeSize;
		}

		@Override
		public int getCount() {
			return DayView.this.endInterval - DayView.this.startInterval;
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
			View view = DayView.this.getAdapter() == null ? convertView : DayView.this.getAdapter().getView(position,
					convertView, parent);
			ViewHolder holder;
			if (view == null) {
				view = View.inflate(DayView.this.getContext(), R.layout.simple_day_item, null);
				holder = new ViewHolder();
				holder.dayTime = (TextView) view.findViewById(R.id.day_time);
				LinearLayout ll = (LinearLayout) view.findViewById(R.id.llEvent);
				holder.event = new TextView[EVENT_COUNT];
				for (int i = 0; i < ll.getChildCount(); i++) {
					holder.event[i] = (TextView) ll.getChildAt(i);
					holder.event[i].setVisibility(View.GONE);
				}
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if (holder.dayTime != null) {
				holder.dayTime.setText(DayView.this.dayTimes[position]);
			}

			if (DayView.this.isPastDays[position]) {
				view.setBackgroundColor(Color.LTGRAY);
			} else {
				view.setBackgroundColor(Color.WHITE);
			}

			// show event
			int width;
			for (int i = 0; i < EVENT_COUNT; i++) {
				int val = dayEvents[position][i];
				// gia tri khac 0, co event
				android.widget.LinearLayout.LayoutParams lp = ((android.widget.LinearLayout.LayoutParams) holder.event[i]
						.getLayoutParams());
				if (val != 0) {
					if (val < 0) {
						// gia tri nho hon ko, bat dau event, them margin top cho view
						lp.topMargin = padding;
						val = -val;
					}
					// hien event
					holder.event[i].setVisibility(View.VISIBLE);
					width = (fullWidth - (padding * val)) / val;
					// thay doi width, dua vao val
					lp.width = width;

					// invisible nhung event trc do de tao khoang trong
					int j = i;
					while (j > 0 && dayEvents[position][j - 1] == 0) {
						j--;
						holder.event[j].setVisibility(View.INVISIBLE);
						holder.event[j].getLayoutParams().width = width;
					}
				}
			}
			view.setTag(holder);
			return view;
		}

		class ViewHolder {
			TextView dayTime, event[];
		}
	}
}
