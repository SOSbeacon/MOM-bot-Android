package org.cnc.msrobot.activity;

import org.cnc.mombot.R;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.DateTimeFormater;

import android.app.Activity;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationActivity extends Activity {
	private RelativeLayout rlNotify, rlContent;
	private TextView tvTitle, tvDesc, tvTime;
	public static final String EXTRA_CONTENT = "EXTRA_CONTENT";
	private Ringtone ringtone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set FLAG to activity can show in lock screen
		Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.activity_notification);
		setUpWindow();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// play default alarm sound
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
			ringtone.play();
		} catch (Exception ex) {
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (ringtone != null) {
			ringtone.stop();
		}
	}

	public void setUpWindow() {
		// find view in activity
		rlNotify = (RelativeLayout) findViewById(R.id.rlNotify);
		rlContent = (RelativeLayout) findViewById(R.id.rlContent);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvDesc = (TextView) findViewById(R.id.tvDesc);
		tvTime = (TextView) findViewById(R.id.tvTime);

		// set click outside content, close activity
		rlNotify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		findViewById(R.id.btnClose).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// get content bundle and set it to textview
		String content = getIntent().getExtras().getString(EXTRA_CONTENT);
		if (!TextUtils.isEmpty(content)) {
			EventResource event = new EventResource(content);
			tvTitle.setText(event.title);
			tvTime.setText(DateTimeFormater.time24hFormater.format(event.start()) + "  -  "
					+ DateTimeFormater.time24hFormater.format(event.end()));
			tvDesc.setText(event.content);
		}

		// Gets the display size so that you can set the window to a percent of that
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		// set width and height to content layout
		if (height > width) {
			rlContent.getLayoutParams().width = (int) (width * .8);
		} else {
			rlContent.getLayoutParams().width = (int) (width * .5);
		}
	}
}
