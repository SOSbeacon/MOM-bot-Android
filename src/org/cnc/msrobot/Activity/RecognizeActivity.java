package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.List;

import org.cnc.msrobot.R;
import org.cnc.msrobot.utils.SpeechToText.SpeechToTextCallback;
import org.cnc.msrobot.utils.SpeechToText.SpeechToTextListener;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class RecognizeActivity extends BaseActivity implements OnClickListener, SpeechToTextListener,
		SpeechToTextCallback {
	private static final int DELAY_SHOW_RMSDB = 200;
	public static final String EXTRA_TEXT = "EXTRA_TEXT";
	public static final String EXTRA_HANDLER = "EXTRA_HANDLER";
	private EditText mEditText;
	private ImageButton mBtnMic;
	private Button mBtnOk, mBtnDelete;
	private List<Drawable> mVolumeLevels;
	private float mCurrentRmsDb = 0;
	final Handler handler = new Handler();
	private Messenger mMessenger;
	final Runnable runVolumn = new Runnable() {
		@Override
		public void run() {
			if (mVolumeLevels != null) {
				int vol = Math.round(mCurrentRmsDb);
				// vol = -2 -> 10. I addition 2
				vol += 2;
				// vol / 2 -> vol level
				vol /= 2;
				if (vol > 6) vol = 6;
				else if (vol < 0) vol = 0;
				mBtnMic.setImageDrawable(mVolumeLevels.get(vol));
				handler.postDelayed(this, DELAY_SHOW_RMSDB);
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make us non-modal, so that others can receive touch events.
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);

		// ...but notify us that it happened.
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		setContentView(R.layout.activity_recognize);

		mEditText = (EditText) findViewById(R.id.etRecognize);
		mBtnMic = (ImageButton) findViewById(R.id.btnMic);
		mBtnOk = (Button) findViewById(R.id.btnOk);
		mBtnDelete = (Button) findViewById(R.id.btnDelete);

		mBtnMic.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);

		// init volumn levels
		Resources mRes = getResources();
		mVolumeLevels = new ArrayList<Drawable>();
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level0));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level1));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level2));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level3));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level4));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level5));
		mVolumeLevels.add(mRes.getDrawable(R.drawable.speak_now_level6));

		// get Handler
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mMessenger = bundle.getParcelable(EXTRA_HANDLER);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		mStt.setListener(this);
		mStt.setCallback(this);
		mStt.listen();
	}

	private void addText(String text) {
		mEditText.setText(mEditText.getText().toString() + text);
		mEditText.setSelection(mEditText.getText().length());
		mEditText.requestFocus();
	}

	private void deleteText() {
		String text = mEditText.getText().toString();
		if (text.length() == 0) return;
		int pos = text.length() - 1;
		while (pos > 0 && text.charAt(pos) != ' ') {
			pos--;
		}
		text = text.substring(0, pos);
		mEditText.setText(text);
		mEditText.setSelection(mEditText.getText().length());
		mEditText.requestFocus();
	}

	private void finishRec() {
		handler.removeCallbacks(runVolumn);
		mStt.stopListening();
		String text = mEditText.getText().toString().trim();
		Intent intent = new Intent();
		intent.putExtra(EXTRA_TEXT, text);
		setResult(Activity.RESULT_OK, intent);
		if (mMessenger != null) {
			Message msg = Message.obtain();
			msg.what = 0;
			msg.obj = text;
			try {
				mMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onBackPressed() {
		finishRec();
		super.onBackPressed();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// If we've received a touch notification that the user has touched
		// outside the app, finish the activity.
		if (MotionEvent.ACTION_OUTSIDE == event.getAction()) { return true; }

		// Delegate everything else to Activity.
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnMic:
				if (!mStt.isRecording) {
					mStt.listen();
				} else {
					mStt.stopListening();
				}
				break;
			case R.id.btnOk:
				finishRec();
				finish();
				break;
			case R.id.btnDelete:
				deleteText();
				break;
			default:
				break;
		}
	}

	@Override
	public void onSpeechStart() {
		handler.postDelayed(runVolumn, DELAY_SHOW_RMSDB);
	}

	@Override
	public void onSpeechStop(int error) {
		handler.removeCallbacks(runVolumn);
		if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
			mStt.listen();
		}
	}

	@Override
	public void onRecognize(ArrayList<String> data) {
		addText(data.get(0) + ". ");
		mStt.listen();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		mCurrentRmsDb = rmsdB;
	}
}
