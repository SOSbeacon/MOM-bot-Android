package android.lib.schedule;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.lib.scheduleview.R;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class ScheduleView<T extends Adapter> extends FrameLayout {
	public interface OnNextClickListener {
		void onNextClick();
	}

	public interface OnPreviousClickListener {
		void onPreviousClick();
	}

	View header;

	private TextView headerTitle;
	private ImageView previousButton;
	private ImageView nextButton;

	private OnNextClickListener onNextClickListener;
	private OnPreviousClickListener onPreviousClickListener;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;

	protected ScheduleView(final Context context) {
		super(context);

		this.initialize(null);
	}

	protected ScheduleView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.initialize(this.getContext().obtainStyledAttributes(attrs, R.styleable.ScheduleView));
	}

	protected ScheduleView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		this.initialize(this.getContext().obtainStyledAttributes(attrs, R.styleable.ScheduleView, defStyle, 0));
	}

	/**
	 * Returns the background drawable of the header.
	 * 
	 * @return the background drawable of the header.
	 */
	public final Drawable getHeaderBackground() {
		return this.header.getBackground();
	}

	/**
	 * Sets a drawable for the header background.
	 * 
	 * @param drawable
	 *            the background drawable to set.
	 */
	public final void setHeaderBackgroundDrawable(final Drawable drawable) {
		this.header.setBackgroundDrawable(drawable);
	}

	/**
	 * Sets a color for the header background.
	 * 
	 * @param color
	 *            the background color to set.
	 */
	public final void setHeaderBackgroundColor(final int color) {
		this.header.setBackgroundColor(color);
	}

	/**
	 * Sets a resource for the header background.
	 * 
	 * @param resource
	 *            the background resource identifier to set.
	 */
	public final void setHeaderBackgroundResource(final int resource) {
		this.header.setBackgroundResource(resource);
	}

	/**
	 * Returns the title text.
	 * 
	 * @return the title text.
	 */
	public final CharSequence getTitle() {
		return this.headerTitle.getText();
	}

	/**
	 * Sets the title text.
	 * <p>
	 * Calling {@link #setCurrentDate(Date)} will reset the title text. Call {@link #setTitle(CharSequence)} again to
	 * set the title text.
	 * </p>
	 * 
	 * @param title
	 *            the string to set.
	 */
	public final void setTitle(final CharSequence title) {
		this.headerTitle.setText(title);
	}

	/**
	 * Sets the title text.
	 * <p>
	 * Calling {@link #setCurrentDate(Date)} will reset the title text. Call {@link #setTitle(int)} again to set the
	 * title text.
	 * </p>
	 * 
	 * @param resource
	 *            the resource identifier to set.
	 */
	public final void setTitle(final int resource) {
		this.headerTitle.setText(resource);
	}

	/**
	 * Returns the size (in pixels) of the default text size in this TextView.
	 * 
	 * @return the size (in pixels) of the default text size in this TextView.
	 */
	public final float getTitleSize() {
		return this.headerTitle.getTextSize();
	}

	/**
	 * Sets the title text size.
	 * 
	 * @param size
	 *            the scaled pixel size.
	 */
	public final void setTitleSize(final float size) {
		this.headerTitle.setTextSize(size);
	}

	/**
	 * Sets the title text size.
	 * 
	 * @param unit
	 *            the desired dimension unit.
	 * @param size
	 *            the desired size in the given units.
	 */
	public final void setTitleSize(final int unit, final float size) {
		this.headerTitle.setTextSize(unit, size);
	}

	/**
	 * Sets the title color for all the states (normal, selected, focused) to be this color.
	 * 
	 * @param color
	 *            the title color to set.
	 */
	public final void setTitleColor(final int color) {
		this.headerTitle.setTextColor(color);
	}

	/**
	 * Sets the title color, size, style, hint color, and highlight color from the specified TextAppearance resource.
	 * 
	 * @param the
	 *            TextAppearance resource to set.
	 */
	public final void setTitleAppearance(final int resource) {
		this.headerTitle.setTextAppearance(this.getContext(), resource);
	}

	/**
	 * Returns the drawable of the "previous" button.
	 * 
	 * @return the drawable of the "previous" button.
	 */
	public final Drawable getPreviousButtonDrawawable() {
		return this.previousButton.getDrawable();
	}

	/**
	 * Sets a drawable for the "previous" button.
	 * 
	 * @param drawable
	 *            the drawable to set.
	 */
	public final void setPreviousButtonDrawable(final Drawable drawable) {
		this.previousButton.setImageDrawable(drawable);
	}

	/**
	 * Sets a bitmap for the "previous" button.
	 * 
	 * @param bitmap
	 *            the bitmap to set.
	 */
	public final void setPreviousButtonBitmap(final Bitmap bitmap) {
		this.previousButton.setImageBitmap(bitmap);
	}

	/**
	 * Sets a resource for the "previous" button.
	 * 
	 * @param resource
	 *            the resource identifier to set.
	 */
	public final void setPreviousButtonResource(final int resource) {
		this.previousButton.setImageResource(resource);
	}

	/**
	 * Returns the {@link OnPreviousClickListner} associated with this {@link MonthView}.
	 * 
	 * @return the {@link OnPreviousClickListner} associated with this {@link MonthView}.
	 */
	public final OnPreviousClickListener getOnPreviousClickListener() {
		return this.onPreviousClickListener;
	}

	/**
	 * Sets the {@link OnPreviousClickListner} callback when the "previous" button is clicked.
	 * 
	 * @param onPreviousClickListener
	 *            callback when the "previous" button is clicked.
	 */
	public final void setOnPreviousClickListener(final OnPreviousClickListener onPreviousClickListener) {
		this.onPreviousClickListener = onPreviousClickListener;
	}

	/**
	 * Returns the drawable of the "next" button.
	 * 
	 * @return the drawable of the "next" button.
	 */
	public final Drawable getNextButtonDrawable() {
		return this.nextButton.getDrawable();
	}

	/**
	 * Sets a drawable for the "next" button.
	 * 
	 * @param drawable
	 *            the drawable to set.
	 */
	public final void setNextButtonDrawable(final Drawable drawable) {
		this.nextButton.setImageDrawable(drawable);
	}

	/**
	 * Sets a bitmap for the "next" button.
	 * 
	 * @param bitmap
	 *            the bitmap to set.
	 */
	public final void setNextButtonBitmap(final Bitmap bitmap) {
		this.nextButton.setImageBitmap(bitmap);
	}

	/**
	 * Sets a resource for the "next" button.
	 * 
	 * @param resource
	 *            the resource identifier to set.
	 */
	public final void setNextButtonResource(final int resource) {
		this.nextButton.setImageResource(resource);
	}

	/**
	 * Returns the {@link OnNextClickListener} associated with this {@link MonthView}.
	 * 
	 * @return the {@link OnNextClickListener} associated with this {@link MonthView}.
	 */
	public final OnNextClickListener getOnNextClickListener() {
		return this.onNextClickListener;
	}

	/**
	 * Sets the {@link OnNextClickListener} callback when the "next" button is clicked.
	 * 
	 * @param onNextClickListener
	 *            callback when the "next" button is clicked.
	 */
	public final void setOnNextClickListener(final OnNextClickListener onNextClickListener) {
		this.onNextClickListener = onNextClickListener;
	}

	/**
	 * Returns the current date to display.
	 * <p>
	 * Default is the system time.
	 * </p>
	 * 
	 * @return the current date to display.
	 */
	public abstract Date getCurrentDate();

	/**
	 * Sets the current date and changes the current month to display if necessary.
	 * <p>
	 * Setting the current date will also set the title.
	 * </p>
	 * 
	 * @param currentDate
	 *            the current date to display.
	 */
	public abstract void setCurrentDate(final Date currentDate);

	public final OnItemClickListener getOnItemClickListener() {
		return this.onItemClickListener;
	}

	public final void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public final OnItemLongClickListener getOnItemLongClickListener() {
		return this.onItemLongClickListener;
	}

	public final void setOnItemLongClickListener(final OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	public void addEventDate(Date start, Date end) {
	}
	
	public void cleanEventData() {
	}
	
	public void refreshCalendar() {
	}

	void initialize(final TypedArray array) {
		// Header background, header title, previous/next buttons
		this.header = this.findViewById(R.id.header);
		this.headerTitle = (TextView) this.findViewById(R.id.header_title);
		this.previousButton = (ImageView) this.findViewById(R.id.header_previous);
		this.nextButton = (ImageView) this.findViewById(R.id.header_next);

		this.setCurrentDate(new Date());

		if (array != null) {
			final CharSequence title = array.getText(R.styleable.ScheduleView_title);

			if (!TextUtils.isEmpty(title)) {
				this.headerTitle.setText(array.getText(R.styleable.ScheduleView_title));
			}

			if (array.hasValue(R.styleable.ScheduleView_titleSize)) {
				this.headerTitle.setTextSize(array.getDimensionPixelSize(R.styleable.ScheduleView_titleSize, 0));
			}

			if (array.hasValue(R.styleable.ScheduleView_titleColor)) {
				this.headerTitle.setTextColor(array.getColor(R.styleable.ScheduleView_titleColor, Color.BLACK));
			}

			if (array.hasValue(R.styleable.ScheduleView_previousDrawable)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_previousDrawable);

				if (drawable != null) {
					this.previousButton.setImageDrawable(drawable);
				}
			}

			if (array.hasValue(R.styleable.ScheduleView_nextDrawable)) {
				final Drawable drawable = array.getDrawable(R.styleable.ScheduleView_nextDrawable);

				if (drawable != null) {
					this.nextButton.setImageDrawable(drawable);
				}
			}

			array.recycle();
		}

		this.previousButton.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void onClick(final View view) {
				if (ScheduleView.this.onPreviousClickListener != null) {
					ScheduleView.this.onPreviousClickListener.onPreviousClick();
				}
			}
		});

		this.nextButton.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void onClick(final View view) {
				if (ScheduleView.this.onNextClickListener != null) {
					ScheduleView.this.onNextClickListener.onNextClick();
				}
			}
		});
	}
}
