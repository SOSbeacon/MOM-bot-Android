package org.cnc.msrobot.utils;

import org.cnc.msrobot.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomActionBar extends RelativeLayout implements OnClickListener {
	private static final int INTERVAL_REC_ANIMATION = 300;
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_HOME = 1;
	public static final int TYPE_EMAIL = 2;
	public static final int TYPE_CLASSIC = 3;
	public static final int TYPE_SEND = 4;
	public static final int TYPE_CALENDAR = 5;
	private ImageView imgRec, imgPlay, imgStop, imgNext;
	private TextView tvSMS, tvEmail, tvTitle, tvWeather, tvAction;
	private View rlActionEmail, rlActionHome, rlAction, rlActionCalendar;
	private ProgressBar prgLoading;
	private int mType = TYPE_DEFAULT;
	private boolean mInitial = false;
	private Handler mHandler = new Handler();
	private OnClickListener mListener;

	private Runnable mRecAnimation = new Runnable() {

		@Override
		public void run() {
			if (imgRec.getVisibility() == View.VISIBLE) {
				imgRec.setVisibility(View.GONE);
			} else {
				imgRec.setVisibility(View.VISIBLE);
			}
			mHandler.postDelayed(this, INTERVAL_REC_ANIMATION);
		}
	};

	public CustomActionBar(Context context) {
		super(context);
		init();
	}

	public CustomActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	protected void init() {
		inflate(getContext(), R.layout.action_bar_common_layout, this);
		imgRec = (ImageView) findViewById(R.id.imgRec);
		tvSMS = (TextView) findViewById(R.id.tvSMS);
		tvEmail = (TextView) findViewById(R.id.tvEmail);
		tvWeather = (TextView) findViewById(R.id.tvWeather);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
		rlActionEmail = findViewById(R.id.rlActionEmail);
		rlActionHome = findViewById(R.id.rlActionHome);
		rlAction = findViewById(R.id.rlAction);
		rlActionCalendar = findViewById(R.id.rlCalendarAction);
		tvAction = (TextView) rlAction.findViewById(R.id.tvAction);
		imgPlay = (ImageView) findViewById(R.id.imgPlay);
		imgStop = (ImageView) findViewById(R.id.imgStop);
		imgNext = (ImageView) findViewById(R.id.imgNext);

		tvSMS.setOnClickListener(this);
		tvEmail.setOnClickListener(this);
		tvAction.setOnClickListener(this);

		imgPlay.setOnClickListener(this);
		imgStop.setOnClickListener(this);
		imgNext.setOnClickListener(this);
		rlActionCalendar.findViewById(R.id.tvCalendarDaily).setOnClickListener(this);
		rlActionCalendar.findViewById(R.id.tvCalendarWeekly).setOnClickListener(this);
		rlActionCalendar.findViewById(R.id.tvCalendarMonthly).setOnClickListener(this);

		tvTitle.setText(((Activity) getContext()).getTitle());
		mInitial = true;
		setType(mType);
	}

	/*
	 * Set action bar item click (non-Javadoc)
	 * 
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	public void setOnClickListener(OnClickListener listener) {
		mListener = listener;
	}

	/**
	 * set action bar type, @see CustomActionBar.TYPE_DEFAULT
	 * 
	 * @param type
	 */
	public void setType(int type) {
		mType = type;
		if (!mInitial) return;
		imgRec.setVisibility(View.GONE);
		switch (type) {
			case TYPE_CLASSIC:
				rlActionHome.setVisibility(View.VISIBLE);
				rlActionEmail.setVisibility(View.GONE);
				rlAction.setVisibility(View.GONE);
				break;
			case TYPE_EMAIL:
				rlActionHome.setVisibility(View.GONE);
				rlActionEmail.setVisibility(View.VISIBLE);
				rlAction.setVisibility(View.GONE);
				break;
			case TYPE_SEND:
				rlActionHome.setVisibility(View.GONE);
				rlActionEmail.setVisibility(View.GONE);
				rlAction.setVisibility(View.VISIBLE);
				break;
			case TYPE_CALENDAR:
				rlActionHome.setVisibility(View.GONE);
				rlActionCalendar.setVisibility(View.VISIBLE);
				rlAction.setVisibility(View.GONE);
				break;
			case TYPE_DEFAULT:
			default:
				rlActionHome.setVisibility(View.GONE);
				rlActionEmail.setVisibility(View.GONE);
				rlAction.setVisibility(View.GONE);
				break;

		}
	}

	public void setTitle(CharSequence title) {
		if (tvTitle != null) tvTitle.setText(title);
	}

	public void hideRightLayout() {
		findViewById(R.id.rlRight).setVisibility(View.GONE);
	}

	public void showRightLayout() {
		findViewById(R.id.rlRight).setVisibility(View.VISIBLE);
	}

	public void showProgressBar() {
		if (prgLoading != null) prgLoading.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		if (prgLoading != null) prgLoading.setVisibility(View.GONE);
	}

	public void showRecAnimation() {
		imgRec.setVisibility(View.VISIBLE);
		mHandler.removeCallbacks(mRecAnimation);
		mHandler.postDelayed(mRecAnimation, INTERVAL_REC_ANIMATION);
	}

	public void hideRecAnimation() {
		imgRec.setVisibility(View.GONE);
		mHandler.removeCallbacks(mRecAnimation);
	}

	public void setSMSText(String text) {
		if (TextUtils.isEmpty(text)) {
			tvSMS.setVisibility(View.GONE);
		} else {
			tvSMS.setVisibility(View.VISIBLE);
			tvSMS.setText(text);
		}
	}

	public void setEmailText(String text) {
		if (TextUtils.isEmpty(text)) {
			tvEmail.setVisibility(View.GONE);
		} else {
			tvEmail.setVisibility(View.VISIBLE);
			tvEmail.setText(text);
		}
	}

	/**
	 * set weather text in action bar
	 * 
	 * @param text
	 */
	public void setWeatherText(String text) {
		if (TextUtils.isEmpty(text)) {
			tvWeather.setVisibility(View.GONE);
		} else {
			tvWeather.setVisibility(View.VISIBLE);
			tvWeather.setText(text);
		}
	}

	/**
	 * set right action text in action bar
	 * 
	 * @param text
	 */
	public void setActionText(String text) {
		if (tvAction != null) {
			tvAction.setText(text);
		}
	}

	@Override
	public void onClick(View v) {
		if (mListener != null) {
			mListener.onClick(v);
		}
	}
}
