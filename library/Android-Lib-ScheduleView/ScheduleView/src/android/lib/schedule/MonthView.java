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
import android.widget.TextView;

public class MonthView extends ScheduleView<MonthView.MonthAdapter> {
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM yyyy"); //$NON-NLS-1$

	private GridView weekdaysBackground;
	private WeekdayAdapter weekdayAdapter;
	private GridView gridView;
	private GridAdapter gridAdapter;
	private MonthView.MonthAdapter monthAdapter;

	private Date currentDate;
	private int firstWeekday = Calendar.SUNDAY;
	private int offsetDay;
	private int gridViewHeight;

	private Date[] monthDates = new Date[MonthView.COLUMNS * MonthView.ROWS];
	private String[] monthDays = new String[MonthView.COLUMNS * MonthView.ROWS];
	private boolean[] isPastDays = new boolean[MonthView.COLUMNS * MonthView.ROWS];
	private boolean[] isPastMonths = new boolean[MonthView.COLUMNS * MonthView.ROWS];
	private boolean[] isFutureDays = new boolean[MonthView.COLUMNS * MonthView.ROWS];
	private boolean[] isFutureMonths = new boolean[MonthView.COLUMNS * MonthView.ROWS];
	private Calendar calendar = Calendar.getInstance();

	public MonthView(final Context context) {
		super(context);
	}

	public MonthView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public MonthView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Returns the adapter used by this {@link MonthView}.
	 * 
	 * @return the adapter used by this {@link MonthView}.
	 */
	public final MonthView.MonthAdapter getAdapter() {
		return this.monthAdapter;
	}

	/**
	 * Sets the data and layout for this {@link MonthView}.
	 * 
	 * @param the
	 *            adapter providing data and layout for this {@link MonthView}.
	 */
	public final void setAdapter(final MonthView.MonthAdapter monthAdapter) {
		this.monthAdapter = monthAdapter;

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
	 * 
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

		final int maxDayOfMonth = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		this.calendar.set(Calendar.DATE, 1);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		this.calendar.set(Calendar.MILLISECOND, 0);

		this.offsetDay = this.calendar.get(Calendar.DAY_OF_WEEK) - 1 - (this.firstWeekday - 1);

		this.setTitle(MonthView.DATE_FORMAT.format(this.currentDate));

		// Caches daily information.
		this.calendar.add(Calendar.DATE, -MonthView.this.offsetDay);

		final Date now = new Date();

		for (int i = 0; i < MonthView.COLUMNS * MonthView.ROWS; i++) {
			this.monthDates[i] = new Date(this.calendar.getTimeInMillis());
			this.monthDays[i] = String.valueOf(this.calendar.get(Calendar.DATE));
			this.isPastDays[i] = this.monthDates[i].before(now)
					&& now.getTime() - this.monthDates[i].getTime() > 86400000;
			this.isFutureDays[i] = this.monthDates[i].after(now);
			this.isPastMonths[i] = i >= this.offsetDay;
			this.isFutureMonths[i] = i >= this.offsetDay + maxDayOfMonth;

			this.calendar.add(Calendar.DATE, 1);
		}

		this.setFirstWeekday(this.getFirstWeekday());

		if (this.gridView != null && this.gridAdapter != null) {
			this.gridAdapter.notifyDataSetChanged();
			this.gridView.invalidateViews();
		}
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

		for (int i = 0; i < MonthView.COLUMNS; i++) {
			int week = i + this.firstWeekday - Calendar.SUNDAY;

			if (week >= MonthView.COLUMNS) {
				week -= MonthView.COLUMNS;
			}

			this.weekdayAdapter.setLabel(i, weekdays[week + 1]);
		}
	}

	@Override
	protected final void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		this.gridViewHeight = MeasureSpec.getSize(heightMeasureSpec);
	}

	@Override
	void initialize(final TypedArray array) {
		try {
			if (this.calendar == null) {
				this.monthDates = new Date[MonthView.COLUMNS * MonthView.ROWS];
				this.monthDays = new String[MonthView.COLUMNS * MonthView.ROWS];
				this.isPastDays = new boolean[MonthView.COLUMNS * MonthView.ROWS];
				this.isPastMonths = new boolean[MonthView.COLUMNS * MonthView.ROWS];
				this.isFutureDays = new boolean[MonthView.COLUMNS * MonthView.ROWS];
				this.isFutureMonths = new boolean[MonthView.COLUMNS * MonthView.ROWS];
				this.calendar = Calendar.getInstance();
			}

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

			final View root = View.inflate(this.getContext(), R.layout.month, this);

			super.initialize(array);

			// Month view
			this.gridView = (GridView) root.findViewById(R.id.month);
			this.gridAdapter = new GridAdapter();
			this.gridView.setAdapter(this.gridAdapter);

			this.setFirstWeekday(Calendar.SUNDAY);

			this.weekdaysBackground = (GridView) root.findViewById(R.id.weekdays);
			this.weekdaysBackground.setAdapter(this.weekdayAdapter);

			this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
					if (MonthView.this.getOnItemClickListener() != null) {
						MonthView.this.getOnItemClickListener().onItemClick(null, view, position,
								MonthView.this.getAdapter().getItemId(position));
					}
				}
			});

			this.gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position,
						final long id) {
					return MonthView.this.getOnItemLongClickListener() == null ? false : MonthView.this
							.getOnItemLongClickListener().onItemLongClick(null, view, position,
									MonthView.this.getAdapter().getItemId(position));
				}
			});
		} catch (final Exception e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
		}
	}

	/**
	 * The base adapter to provide basic functions for a {@link MonthView}, such as {@link #getDate(int)} for getting a
	 * <code>Date</code> object based on the position of a day, and {@link #isPastDate(int)} for determining whether the
	 * given position of a day corresponds to a past date.
	 * <p>
	 * If the implementations of {@link MonthAdapter#getView(int, View, ViewGroup)} returns a view containing a
	 * <code>TextView</code> with ID equals {@link R.id.month_day}, the <code>TextView</code> will display the day
	 * number automatically.
	 * </p>
	 */
	public abstract class MonthAdapter extends BaseAdapter {
		protected MonthAdapter() {
		}

		/**
		 * Returns the number of days to display.
		 * <p>
		 * 7 columns x 6 rows = 42.
		 * </p>
		 * 
		 * @return 42.
		 */
		@Override
		public final int getCount() {
			return MonthView.COLUMNS * MonthView.ROWS;
		}

		/**
		 * Returns a <code>Date</code> object based on the given <code>position<code> of a day.
		 * 
		 * @param position
		 *            the position of a day to get a <code>Date</code> object.
		 *            <p>
		 *            The value of <code>position</code> lies between 0 and 41 inclusive, counting horizontally, with
		 *            the top left cell being 0 and the bottom right cell being 41.
		 *            </p>
		 * @return a <code>Date</code> object reflecting the given <code>position</code> of a day.
		 */
		@SuppressWarnings("synthetic-access")
		public final Date getDate(final int position) {
			return MonthView.this.monthDates[position];
		}

		/**
		 * Indicates whether the given <code>position</code> corresponds to a past date.
		 * 
		 * @param position
		 *            the position of a day to check.
		 *            <p>
		 *            The value of <code>position</code> lies between 0 and 41 inclusive, counting horizontally, with
		 *            the top left cell being 0 and the bottom right cell being 41.
		 *            </p>
		 * @return <code>true</code> if the given <code>position</code> corresponds to a past date; otherwise,
		 *         <code>false</code>.
		 */
		@SuppressWarnings("synthetic-access")
		public final boolean isPastDate(final int position) {
			return MonthView.this.isPastDays[position];
		}

		/**
		 * Indicates whether the given <code>position</code> corresponds to a day of past month.
		 * 
		 * @param position
		 *            the position of a day to check.
		 *            <p>
		 *            The value of <code>position</code> lies between 0 and 41 inclusive, counting horizontally, with
		 *            the top left cell being 0 and the bottom right cell being 41.
		 *            </p>
		 * @return <code>true</code> if the given <code>position</code> corresponds to a day of past month; otherwise,
		 *         <code>false</code>.
		 */
		@SuppressWarnings("synthetic-access")
		public final boolean isPastMonth(final int position) {
			return MonthView.this.isPastMonths[position];
		}

		/**
		 * Indicates whether the given <code>position</code> corresponds to a future day.
		 * 
		 * @param position
		 *            the position of a day to check.
		 *            <p>
		 *            The value of <code>position</code> lies between 0 and 41 inclusive, counting horizontally, with
		 *            the top left cell being 0 and the bottom right cell being 41.
		 *            </p>
		 * @return <code>true</code> if the given <code>position</code> corresponds to a future date; otherwise,
		 *         <code>false</code>.
		 */
		@SuppressWarnings("synthetic-access")
		public final boolean isFutureDate(final int position) {
			return MonthView.this.isFutureDays[position];
		}

		/**
		 * Indicates whether the given <code>position</code> corresponds to a day of future month.
		 * 
		 * @param position
		 *            the position of a day to check.
		 *            <p>
		 *            The value of <code>position</code> lies between 0 and 41 inclusive, counting horizontally, with
		 *            the top left cell being 0 and the bottom right cell being 41.
		 *            </p>
		 * @return <code>true</code> if the given <code>position</code> corresponds to a day of future month; otherwise,
		 *         <code>false</code>.
		 */
		@SuppressWarnings("synthetic-access")
		public final boolean isFutureMonth(final int position) {
			return MonthView.this.isFutureMonths[position];
		}
	}

	private final class WeekdayAdapter extends BaseAdapter {
		private final Context context;
		private final Drawable[] backgrounds = new Drawable[MonthView.COLUMNS];
		private final CharSequence[] labels = new CharSequence[MonthView.COLUMNS];

		public WeekdayAdapter(final Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return MonthView.COLUMNS;
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

			if (holder.background != null) {
				holder.background.setBackgroundDrawable(this.backgrounds[position]);
			}

			if (holder.label != null) {
				holder.label.setText(this.labels[position]);
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
		public GridAdapter() {
		}

		/**
		 * Returns the number of days to display.
		 * <p>
		 * 7 columns x 6 rows = 42.
		 * </p>
		 * 
		 * @return 42.
		 */
		@Override
		public int getCount() {
			return MonthView.COLUMNS * MonthView.ROWS;
		}

		@Override
		public Object getItem(final int position) {
			return Integer.valueOf(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			// Inflates the view for a day item. If this is null, inflates the default layout.
			View view = MonthView.this.getAdapter() == null ? null : MonthView.this.getAdapter().getView(position,
					convertView, parent);

			if (view == null) {
				view = View.inflate(MonthView.this.getContext(), R.layout.simple_month_item, null);
			}

			// Displays the day number if R.id.month_day is found.
			final TextView monthDay = (TextView) view.findViewById(R.id.month_day);

			if (MonthView.this.isPastDays[position]) {
				view.setBackgroundColor(Color.LTGRAY);
			} else {
				view.setBackgroundColor(Color.WHITE);

				final Date date = MonthView.this.monthDates[position];
				final Calendar calendar = Calendar.getInstance();

				calendar.setTime(date);

				final Drawable drawable = MonthView.this.weekdayAdapter.backgrounds[calendar.get(Calendar.DAY_OF_WEEK) - 1];

				if (drawable != null) {
					view.setBackgroundDrawable(drawable);
				}
			}

			if (monthDay != null) {
				monthDay.setText(MonthView.this.monthDays[position]);

				if (MonthView.this.isPastDays[position]) {
					monthDay.setTextColor(Color.DKGRAY);
				} else {
					monthDay.setTextColor(Color.BLACK);
				}
			}

			// Adjusts the item height to fit all 6 rows into the available space.
			view.setLayoutParams(new GridView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					(MonthView.this.gridViewHeight - (MonthView.this.weekdaysBackground.getHeight() + MonthView.this.header
							.getHeight())) / MonthView.ROWS));

			return view;
		}
	}
}
