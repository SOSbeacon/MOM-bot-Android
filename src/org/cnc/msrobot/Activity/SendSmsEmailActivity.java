package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cnc.msrobot.R;
import org.cnc.msrobot.task.SendEmailTask;
import org.cnc.msrobot.utils.ContactsEditText;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class SendSmsEmailActivity extends BaseActivity implements OnClickListener {
	public static final int TYPE_SENT_SMS = 0;
	public static final int TYPE_SENT_EMAIL = 1;
	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_TO = "extra_to";
	public static final String EXTRA_SUBJECT = "extra_subject";
	public static final String EXTRA_BODY = "extra_body";
	public static final String EXTRA_IMAGE = "extra_image";
	private Button mBtnSend, mBtnClose;
	private EditText mEtSubject, mEtBody;
	private ContactsEditText mEtTo;
	private ImageView mImgAttachment;
	private int mType;
	private String image;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_sms_mail);

		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnClose = (Button) findViewById(R.id.btnClose);
		mBtnSend.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);

		mEtTo = (ContactsEditText) findViewById(R.id.etTo);
		mEtSubject = (EditText) findViewById(R.id.etSubject);
		mEtBody = (EditText) findViewById(R.id.etBody);
		mImgAttachment = (ImageView) findViewById(R.id.imgAttachment);

		Bundle bundle = getIntent().getExtras();
		mType = bundle.getInt(EXTRA_TYPE);
		image = bundle.getString(EXTRA_IMAGE);
		if (!TextUtils.isEmpty(image)) {
			Log.d("zzz", image);
			mImgAttachment.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage("file:/" + image, mImgAttachment);
		}
		if (mType == TYPE_SENT_SMS) {
			mEtSubject.setVisibility(View.GONE);
			findViewById(R.id.tvSubject).setVisibility(View.GONE);
			mEtTo.setShowNumber(true);
		} else {
			mEtSubject.setText(bundle.getString(EXTRA_SUBJECT));
			mEtTo.setShowEmail(true);
		}
		mEtTo.setText(bundle.getString(EXTRA_TO));
		mEtBody.setText(bundle.getString(EXTRA_BODY));

		if (!TextUtils.isEmpty(mEtBody.getText().toString())) {
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					String msg = "";
					if (mType == TYPE_SENT_SMS) {
						msg = getString(R.string.recognize_ask_send_message);
					} else {
						msg = getString(R.string.recognize_ask_send_email);
					}
					speakBeforeRecognize(msg, 0);
				}
			}, 1000);
		}
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				listen();
			}
		}, 500);
	}

	@Override
	public void onRecognize(ArrayList<String> data) {
		String answer = data.get(0);
		String yes = getString(R.string.common_yes).toLowerCase(Locale.US);
		showCenterToast(getString(R.string.common_answer, answer));
		if (yes.equals(answer.toLowerCase(Locale.US))) {
			onClick(mBtnSend);
		}
	}

	protected boolean validateEmail(String email) {
		boolean valid = false;
		if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) valid = true;
		return valid;
	}

	@Override
	public void onClick(View v) {

		if (TextUtils.isEmpty(mEtTo.getText())) return;
		switch (v.getId()) {
			case R.id.btnClose:
				finish();
				break;
			case R.id.btnSend:
				if (mType == TYPE_SENT_SMS) {
					SmsManager smsManager = SmsManager.getDefault();
					// Pattern patternNumber = Pattern.compile("(^(\\+)?\\d{2}-\\d{8,16})|(0\\d{8,18})|(\\d{8,18})");
					Matcher m = Patterns.PHONE.matcher(mEtTo.getText().toString());
					while (m.find()) { // Find each match in turn; String can't do this.
						String number = m.group(); // Access a submatch group; String can't do this.
						smsManager.sendTextMessage(number, null, mEtBody.getText().toString(), null, null);
					}
					showCenterToast("Send SMS success!");
				} else {
					try {
						String emailTo = "";
						Matcher m = Patterns.EMAIL_ADDRESS.matcher(mEtTo.getText().toString());
						while (m.find()) { // Find each match in turn; String can't do this.
							emailTo += "," + m.group();
						}
						if (!TextUtils.isEmpty(emailTo)) {
							emailTo = emailTo.substring(1);
						}
						new SendEmailTask(this, emailTo, mEtSubject.getText().toString(), mEtBody.getText().toString(),
								image).execute();
						showCenterToast("Send Email success!");
					} catch (Exception e) {
						showCenterToast(e.getMessage());
						e.printStackTrace();
					}
				}
				finish();
			default:
				break;
		}
	}
}
