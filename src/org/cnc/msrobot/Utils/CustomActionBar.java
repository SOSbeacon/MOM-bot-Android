package org.cnc.msrobot.utils;

import java.util.Calendar;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomActionBar extends RelativeLayout implements OnClickListener {
	private static final int INTERVAL_TIMER = 1000;
	private static final int INTERVAL_REC_ANIMATION = 500;
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_HOME = 1;
	private ImageView imgSearch, imgAlarm, imgRec;
	private TextView tvTimer, tvTitle;
	private ProgressBar prgLoading;
	private int mType = TYPE_DEFAULT;
	private boolean mInitial = false;
	private Handler mHandler = new Handler();

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (tvTimer != null) {
				Calendar calendar = Calendar.getInstance();
				String sTime = DateTimeFormater.timeFullFormater.format(calendar.getTime());
				tvTimer.setText(sTime);
			}
			mHandler.postDelayed(this, INTERVAL_TIMER);
		}
	};
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
		imgSearch = (ImageView) findViewById(R.id.imgSearch);
		imgAlarm = (ImageView) findViewById(R.id.imgAlarm);
		imgRec = (ImageView) findViewById(R.id.imgRec);
		tvTimer = (TextView) findViewById(R.id.tvTimer);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		prgLoading = (ProgressBar) findViewById(R.id.prgLoading);

		imgSearch.setOnClickListener(this);
		tvTimer.setOnClickListener(this);
		imgAlarm.setOnClickListener(this);

		mHandler.postDelayed(mUpdateTimeTask, INTERVAL_TIMER);
		tvTitle.setText(((Activity) getContext()).getTitle());
		mInitial = true;
		setType(mType);
	}

	public void setType(int type) {
		mType = type;
		if (!mInitial) return;
		switch (type) {
			case TYPE_DEFAULT:
				imgSearch.setVisibility(View.GONE);
				imgAlarm.setVisibility(View.GONE);
				imgRec.setVisibility(View.GONE);
				tvTimer.setVisibility(View.GONE);
				break;
			case TYPE_HOME:
				imgSearch.setVisibility(View.VISIBLE);
				imgAlarm.setVisibility(View.VISIBLE);
				imgRec.setVisibility(View.GONE);
				tvTimer.setVisibility(View.VISIBLE);
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
		mHandler.postDelayed(mRecAnimation, INTERVAL_REC_ANIMATION);
	}

	public void hideRecAnimation() {
		imgRec.setVisibility(View.GONE);
		mHandler.removeCallbacks(mRecAnimation);
	}

	@Override
	public void onClick(View v) {
		MainActivity activity = (MainActivity) getContext();
		switch (v.getId()) {
			case R.id.tvTimer:
				if (activity.getTextToSpeech() != null) {
					activity.getTextToSpeech().speak(AppUtils.getCurrentTimeForSpeech(getContext()),
							TextToSpeech.QUEUE_FLUSH, null);
				}
				break;
			case R.id.imgSearch:
				activity.listen(MainActivity.REC_SEARCH);
				break;
			case R.id.imgAlarm:
				activity.listen(MainActivity.REC_ALARM);
				break;
			default:
				break;
		}
	}
}
