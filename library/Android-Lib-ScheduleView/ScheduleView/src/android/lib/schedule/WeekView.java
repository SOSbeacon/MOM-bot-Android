package android.lib.schedule;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

public class WeekView extends ScheduleView<WeekView.WeekAdapter> {
	private static final int INTERVAL = 60;
	private static final int START_INTERVAL = 0;
	private static final int END_INTERVAL = 24;
	private static final int COLUMNS = 8;
	private static final int ROWS = WeekView.END_INTERVAL - WeekView.START_INTERVAL;
	private static final int EVENT_COUNT = 3;

	private static final String WEEK_FORMAT = "%1$s %2$s"; //$NON-NLS-1$
	private static final DateFormat DAY_FORMAT = new SimpleDateFormat("d"); //$NON-NLS-1$
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy"); //$NON-NLS-1$
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
	private static final String HEADER_FORMAT = "%1$s - %2$s"; //$NON-NLS-1$

	private GridView weekdaysBackground;
	private WeekdayAdapter weekdayAdapter;
	private GridView gridView;
	private GridAdapter gridAdapter;
	private WeekView.WeekAdapter weekAdapter;

	private Date currentDate;
	private int firstWeekday = Calendar.SUNDAY;
	private int offsetDay;

	private Date[] weekDates = new Date[(WeekView.COLUMNS - 1) * WeekView.ROWS];
	private String[] weekTimes = new String[WeekView.ROWS];
	private boolean[] isPastDays = new boolean[(WeekView.COLUMNS - 1) * WeekView.ROWS];
	private Calendar calendar = Calendar.getInstance();
	private int interval = WeekView.INTERVAL;
	private int startInterval;
	private int endInterval;
	private int[][][] dayEvents = new int[COLUMNS - 1][ROWS][EVENT_COUNT];
	private int[] eventCount = new int[COLUMNS - 1];

	public WeekView(final Context context) {
		super(context);
	}

	public WeekView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public WeekView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Returns the adapter used by this {@link WeekView}.
	 * 
	 * @return the adapter used by this {@link WeekView}.
	 */
	public final WeekView.WeekAdapter getAdapter() {
		return this.weekAdapter;
	}

	/**
	 * Sets the data and layout for this {@link WeekView}.
	 * 
	 * @param the
	 *            adapter providing data and layout for this {@link WeekView}.
	 */
	public final void setAdapter(final WeekView.WeekAdapter weekAdapter) {
		this.weekAdapter = weekAdapter;

		this.gridView.setAdapter(this.gridAdapter);

		this.requestLayout();
	}

	/**
	 * Sets the background drawable to use for a weekday name.
	 * 
	 * @param weekday
	 *            the weekday to set background drawable to.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @param drawable
	 *            the drawable to set.
	 */
	public final void setWeekdayBackgroundDrawable(final int weekday, final Drawable drawable) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		this.weekdayAdapter.setBackground(weekday - 1, drawable);
	}

	/**
	 * Sets the background color to use for a weekday name.
	 * 
	 * @param weekday
	 *            the weekday to set background color to.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @param color
	 *            the color to set.
	 */
	public final void setWeekdayBackgroundColor(final int weekday, final int color) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		this.weekdayAdapter.setBackground(weekday - 1, new ColorDrawable(color));
	}

	/**
	 * Sets the background to use for a weekday name.
	 * 
	 * @param weekday
	 *            the weekday to set background to.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @param resource
	 *            a drawable resource identifier.
	 */
	public final void setWeekdayBackgroundResource(final int weekday, final int resource) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		this.weekdayAdapter.setBackground(weekday - 1, this.getResources().getDrawable(resource));
	}

	/**
	 * Returns the weekday label.
	 * 
	 * @param weekday
	 *            the weekday to get the label from.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @return a string label.
	 */
	public final CharSequence getWeekdayLabel(final int weekday) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		return this.weekdayAdapter.getLabel(weekday - 1);
	}

	/**
	 * Sets a weekday label.
	 * <p>
	 * Calling {@link #setFirstWeekday(int)} will reset the weekday labels. Call
	 * {@link #setWeekdayLabelText(int, CharSequence)} again to set the weekday labels.
	 * </p>
	 * 
	 * @param weekday
	 *            the weekday to set label to.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @param resource
	 *            a string label.
	 */
	public final void setWeekdayLabelText(final int weekday, final CharSequence label) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		this.weekdayAdapter.setLabel(weekday - 1, label);
	}

	/**
	 * Sets a weekday label.
	 * <p>
	 * Calling {@link #setFirstWeekday(int)} will reset the weekday labels. Call
	 * {@link #setWeekdayLabelResource(int, int)} again to set the weekday labels.
	 * </p>
	 * 
	 * @param weekday
	 *            the weekday to set label to.
	 *            <p>
	 *            Value must be one of {@link Calendar#SUNDAY}, {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
	 *            {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY}, {@link Calendar#FRIDAY} or
	 *            {@link Calendar#SATURDAY}.
	 *            </p>
	 * @param resource
	 *            a string resource identifier.
	 */
	public final void setWeekdayLabelResource(final int weekday, final int resource) {
		if (weekday < Calendar.SUNDAY || weekday > Calendar.SATURDAY) { throw new IllegalArgumentException(
				"Unrecognized weekday value"); //$NON-NLS-1$
		}

		this.weekdayAdapter.setLabel(weekday - 1, this.getContext().getText(resource));
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

	/**
	 * Returns the current date to display.
	 * <p>
	 * Default is the system time.
	 * </p>
	 * 
	 * @return the current date to display.
	 */
	@Override
	public final Date getCurrentDate() {
		return this.currentDate;
	}

	/**
	 * Sets the current date and changes the current month to display if necessary.
	 * <p>
	 * Setting the current date will also set the title.
	 * </p>
	 * 
	 * @param currentDate
	 *            the current date to display.
	 */
	@Override
	public final void setCurrentDate(final Date currentDate) {
		this.currentDate = currentDate;

		this.calendar.setTime(currentDate);

		this.calendar.set(Calendar.HOUR_OF_DAY, 0);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		this.calendar.set(Calendar.MILLISECOND, 0);

		this.offsetDay = this.calendar.get(Calendar.DAY_OF_WEEK) - 1 - (this.firstWeekday - 1);

		// Caches daily information.
		this.calendar.add(Calendar.DATE, -this.offsetDay + 7 - 1);
		final String toDate = WeekView.DATE_FORMAT.format(this.calendar.getTime());

		this.calendar.add(Calendar.DATE, -6);
		final String fromDate = WeekView.DATE_FORMAT.format(this.calendar.getTime());

		this.setTitle(String.format(WeekView.HEADER_FORMAT, fromDate, toDate));

		final Date now = new Date();

		for (int i = 0; i < (WeekView.COLUMNS - 1) * (this.endInterval - this.startInterval); i++) {
			if (i % (WeekView.COLUMNS - 1) == 0) {
				this.calendar.add(Calendar.MINUTE, this.startInterval * this.interval + this.interval
						* (i / (WeekView.COLUMNS - 1)));

				this.weekTimes[i / (WeekView.COLUMNS - 1)] = WeekView.TIME_FORMAT.format(this.calendar.getTime());
			}

			this.weekDates[i] = new Date(this.calendar.getTimeInMillis());
			this.isPastDays[i] = this.weekDates[i].before(now);

			if (i % (WeekView.COLUMNS - 1) == 6) {
				this.calendar.set(Calendar.HOUR_OF_DAY, 0);
				this.calendar.set(Calendar.MINUTE, 0);

				this.calendar.add(Calendar.DATE, -6);
			} else {
				this.calendar.add(Calendar.DATE, 1);
			}
		}

		this.setFirstWeekday(this.getFirstWeekday());
	}

	/**
	 * Returns the first weekday to display.
	 * <p>
	 * Default is {@link Calendar#SUNDAY}.
	 * </p>
	 * 
	 * @return the first weekday to display.
	 */
	public final int getFirstWeekday() {
		return this.firstWeekday;
	}

	/**
	 * Sets the first weekday to display.
	 * <p>
	 * Setting the first weekday will also set the weekday names. Any weekday names set by calling
	 * {@link #setWeekdayLabelText(int, CharSequence)} or {@link #setWeekdayLabelResource(int, int)} will be overridden.
	 * </p>
	 * 
	 * @param weekday
	 *            the first weekday to display.
	 */
	public final void setFirstWeekday(final int weekday) {
		this.firstWeekday = weekday;

		final String[] weekdays = DateFormatSymbols.getInstance().getShortWeekdays();

		for (int i = 0; i < WeekView.COLUMNS - 1; i++) {
			int week = i + this.firstWeekday - Calendar.SUNDAY;

			if (week >= WeekView.COLUMNS - 1) {
				week -= WeekView.COLUMNS - 1;
			}

			this.weekdayAdapter.setLabel(
					i,
					String.format(WeekView.WEEK_FORMAT, weekdays[week + 1],
							WeekView.DAY_FORMAT.format(this.weekDates[i])));
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
		dayEvents = new int[COLUMNS - 1][ROWS][EVENT_COUNT];
	}

	@Override
	public final void addEventDate(Date start, Date end) {
		// thuat toan them su kien vao mang
		// tim vi tri ngay su kien trong mang weekDates
		int day = start.getDate();
		int month = start.getMonth();
		int year = start.getYear();
		int dayPos = -1;
		for (int i = 0; i < weekDates.length; i++) {
			if (weekDates[i].getDate() == day && weekDates[i].getMonth() == month && weekDates[i].getYear() == year) {
				dayPos = i;
				break;
			}
		}
		if (dayPos == -1) return;
		final int startHour = start.getHours();
		final int endHour = end.getHours();
		int col = 0;
		if (eventCount[dayPos] > 0) {
			// quet tung dong
			for (int i = startHour; i <= endHour; i++) {
				// quet tung cot truoc do tren 1 dong, neu o co gia tri lon hon col thi o do da co event, tang col
				while (col < EVENT_COUNT && col < eventCount[dayPos] && dayEvents[dayPos][i][col] > 0) {
					col++;
				}
			}
		}
		if (col >= EVENT_COUNT) return;
		for (int i = startHour; i <= endHour; i++) {
			fillEventDays(i, col, col + 1, dayPos);
			if (i == startHour) {
				// so dau tien cua event la so am nguoc gia tri
				dayEvents[dayPos][i][col] = -(col + 1);
			}
		}
		eventCount[dayPos]++;
	}

	/**
	 * Ham de quy loan fill nhung o lien quan voi nhau
	 * 
	 * @param x
	 * @param y
	 */
	private void fillEventDays(int x, int y, int value, int dayPos) {
		if (dayEvents[dayPos][x][y] < 0) {
			dayEvents[dayPos][x][y] = -value;
		} else {
			dayEvents[dayPos][x][y] = value;
		}
		if (x > 0 && dayEvents[dayPos][x - 1][y] != 0 && Math.abs(dayEvents[dayPos][x - 1][y]) < value) {
			fillEventDays(x - 1, y, value, dayPos);
		}

		if (x < ROWS - 1 && dayEvents[dayPos][x + 1][y] != 0 && Math.abs(dayEvents[dayPos][x + 1][y]) < value) {
			fillEventDays(x + 1, y, value, dayPos);
		}

		if (y > 0 && dayEvents[dayPos][x][y - 1] != 0 && Math.abs(dayEvents[dayPos][x][y - 1]) < value) {
			fillEventDays(x, y - 1, value, dayPos);
		}

		if (y < EVENT_COUNT - 1 && dayEvents[dayPos][x][y + 1] != 0 && Math.abs(dayEvents[dayPos][x][y + 1]) < value) {
			fillEventDays(x, y + 1, value, dayPos);
		}
	}

	@Override
	void initialize(final TypedArray array) {
		try {
			this.weekdayAdapter = new WeekdayAdapter(this.getContext());

			if (array.hasValue(R.styleable.ScheduleView_sundayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_sundayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.SUNDAY,
							array.getColor(R.styleable.ScheduleView_sundayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.SUNDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_mondayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_mondayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.MONDAY,
							array.getColor(R.styleable.ScheduleView_mondayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.MONDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_tuesdayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_tuesdayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.TUESDAY,
							array.getColor(R.styleable.ScheduleView_tuesdayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.TUESDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_wednesdayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_wednesdayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.WEDNESDAY,
							array.getColor(R.styleable.ScheduleView_wednesdayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.WEDNESDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_thursdayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_thursdayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.THURSDAY,
							array.getColor(R.styleable.ScheduleView_thursdayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.THURSDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_fridayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_fridayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.FRIDAY,
							array.getColor(R.styleable.ScheduleView_fridayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.FRIDAY, drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_saturdayBackground)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_saturdayBackground);

				if (drawable == null) {
					this.setWeekdayBackgroundColor(Calendar.SATURDAY,
							array.getColor(R.styleable.ScheduleView_saturdayBackground, Color.TRANSPARENT));
				} else {
					this.setWeekdayBackgroundDrawable(Calendar.SATURDAY, drawable);
				}
			}

			this.interval = array.getInt(R.styleable.ScheduleView_interval, WeekView.INTERVAL);
			this.startInterval = array.getInt(R.styleable.ScheduleView_startInterval, WeekView.START_INTERVAL);
			this.endInterval = array.getInt(R.styleable.ScheduleView_endInterval, WeekView.END_INTERVAL);

			if (this.calendar == null) {
				this.weekDates = new Date[(WeekView.COLUMNS - 1) * (this.endInterval - this.startInterval)];
				this.weekTimes = new String[this.endInterval - this.startInterval];
				this.isPastDays = new boolean[(WeekView.COLUMNS - 1) * (this.endInterval - this.startInterval)];
				this.calendar = Calendar.getInstance();
			}

			final View root = View.inflate(this.getContext(), R.layout.week, this);

			super.initialize(array);

			// Week wiew
			this.gridView = (GridView) root.findViewById(R.id.week);
			this.gridAdapter = new GridAdapter();
			this.gridView.setAdapter(this.gridAdapter);

			this.setFirstWeekday(Calendar.SUNDAY);

			this.weekdaysBackground = (GridView) root.findViewById(R.id.weekdays);
			this.weekdaysBackground.setAdapter(this.weekdayAdapter);

			this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
					if (WeekView.this.getOnItemClickListener() != null) {
						WeekView.this.getOnItemClickListener().onItemClick(null, view, position,
								WeekView.this.getAdapter().getItemId(position));
					}
				}
			});

			this.gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position,
						final long id) {
					return WeekView.this.getOnItemLongClickListener() == null ? false : WeekView.this
							.getOnItemLongClickListener().onItemLongClick(null, view, position,
									WeekView.this.getAdapter().getItemId(position));
				}
			});
		} catch (final Exception e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
		}
	}

	public abstract class WeekAdapter extends BaseAdapter {
		protected WeekAdapter() {
		}

		/**
		 * Returns the number of days plus 1 blank column on the left to display.
		 * <p>
		 * 7 + 1 columns x 12 hours * 4 quarters = 48.
		 * </p>
		 * 
		 * @return 42.
		 */
		@Override
		public final int getCount() {
			return WeekView.this.endInterval - WeekView.this.startInterval;
		}

		public final Date getDate(final int position) {
			if (position % WeekView.COLUMNS == 0) { return null; }

			return WeekView.this.weekDates[position - position / WeekView.COLUMNS];
		}

		public final boolean isPastDate(final int position) {
			if (position % WeekView.COLUMNS == 0) { return false; }

			return WeekView.this.isPastDays[position - position / WeekView.COLUMNS];
		}
	}

	private final class WeekdayAdapter extends BaseAdapter {
		private final Context context;
		private final Drawable[] backgrounds = new Drawable[WeekView.COLUMNS - 1];
		private final CharSequence[] labels = new CharSequence[WeekView.COLUMNS - 1];

		public WeekdayAdapter(final Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return WeekView.COLUMNS;
		}

		@Override
		public Object getItem(final int position) {
			return Integer.valueOf(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		public void setBackground(final int position, final Drawable drawable) {
			this.backgrounds[position] = drawable;
		}

		public CharSequence getLabel(final int position) {
			return this.labels[position];
		}

		public void setLabel(final int position, final CharSequence label) {
			this.labels[position] = label;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view;
			final ViewHolder holder;

			if (convertView == null) {
				view = View.inflate(this.context, R.layout.weekday, null);
				holder = new ViewHolder(view.findViewById(R.id.weekday_background),
						(TextView) view.findViewById(R.id.weekday_text));

				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			if (position % WeekView.COLUMNS == 0) {
				if (holder.background != null) {
					holder.background.setBackgroundDrawable(null);
				}

				if (holder.label != null) {
					holder.label.setText(null);
				}
			} else {
				if (holder.background != null) {
					holder.background.setBackgroundDrawable(this.backgrounds[position - 1]);
				}

				if (holder.label != null) {
					holder.label.setText(this.labels[position - 1]);
				}
			}

			return view;
		}

		private final class ViewHolder {
			public View background;
			public TextView label;

			public ViewHolder(final View background, final TextView label) {
				this.background = background;
				this.label = label;
			}
		}
	}

	private final class GridAdapter extends BaseAdapter {
		final int padding;
		final int fullWidth;

		public GridAdapter() {
			padding = getResources().getDimensionPixelSize(R.dimen.widget_spacing);
			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			fullWidth = screenWidth / COLUMNS;
		}

		@Override
		public int getCount() {
			return WeekView.COLUMNS * (WeekView.this.endInterval - WeekView.this.startInterval);
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
			View view = WeekView.this.getAdapter() == null ? convertView : WeekView.this.getAdapter().getView(position,
					convertView, parent);

			ViewHolder holder;
			if (view == null) {
				view = View.inflate(WeekView.this.getContext(), R.layout.simple_week_item, null);
				holder = new ViewHolder();
				holder.llEvent = (LinearLayout) view.findViewById(R.id.llEvent);
				holder.event = new TextView[EVENT_COUNT];
				for (int i = 0; i < holder.llEvent.getChildCount(); i++) {
					holder.event[i] = (TextView) holder.llEvent.getChildAt(i);
					holder.event[i].setVisibility(View.GONE);
				}
				holder.weekTime = (TextView) view.findViewById(R.id.week_time);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			int col = position % WeekView.COLUMNS;
			if (holder.llEvent != null) {
				holder.llEvent.setVisibility(View.GONE);
			}
			if (col == 0) {
				if (holder.weekTime != null) {
					holder.weekTime.setText(WeekView.this.weekTimes[position / WeekView.COLUMNS]);
					holder.weekTime.setVisibility(View.VISIBLE);
				}

				view.setBackgroundColor(Color.WHITE);
			} else {
				if (holder.weekTime != null) {
					holder.weekTime.setVisibility(View.GONE);
				}

				if (WeekView.this.isPastDays[position - 1 - position / WeekView.COLUMNS]) {
					view.setBackgroundColor(Color.LTGRAY);
				} else {
					view.setBackgroundColor(Color.WHITE);
				}

				// show event
				int width;
				int row = position / WeekView.COLUMNS;
				boolean showEvent = false;
				for (int i = 0; i < EVENT_COUNT; i++) {
					int val = dayEvents[col - 1][row][i];
					if (val != 0) {
						showEvent = true;
						// gia tri khac 0, co event
						android.widget.LinearLayout.LayoutParams lp = ((android.widget.LinearLayout.LayoutParams) holder.event[i]
								.getLayoutParams());
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
						while (j > 0 && dayEvents[col - 1][row][j - 1] == 0) {
							j--;
							holder.event[j].setVisibility(View.INVISIBLE);
							holder.event[j].getLayoutParams().width = width;
						}
					}
				}
				if (showEvent && holder.llEvent != null) {
					holder.llEvent.setVisibility(View.VISIBLE);
				}
			}
			view.setTag(holder);

			return view;
		}

		class ViewHolder {
			TextView weekTime, event[];
			LinearLayout llEvent;
		}
	}
}
