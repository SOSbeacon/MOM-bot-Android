package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.task.SendEmailTask;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.ContactsEditText;
import org.cnc.msrobot.utils.ContactsEditText.Contact;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DialogUtils.OnConfirmClickListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class SendSmsEmailActivity extends BaseActivity implements OnClickListener {
	public static final String EXTRA_TO = "extra_to";
	public static final String EXTRA_BODY = "extra_body";
	public static final String EXTRA_IMAGE = "extra_image";
	private EditText mEtBody;
	private ContactsEditText mEtTo;
	private ImageView mImgAttachment;
	private CheckBox mChkSms, mChkEmail;
	private String image;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_sms_mail);
		getCusomActionBar().setType(CustomActionBar.TYPE_SEND);
		getCusomActionBar().setOnClickListener(this);

		mEtTo = (ContactsEditText) findViewById(R.id.etTo);
		mEtBody = (EditText) findViewById(R.id.etBody);
		mImgAttachment = (ImageView) findViewById(R.id.imgAttachment);
		mChkSms = (CheckBox) findViewById(R.id.chkSms);
		mChkEmail = (CheckBox) findViewById(R.id.chkEmail);

		// set default send email
		mChkEmail.setChecked(true);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mEtTo.setText(bundle.getString(EXTRA_TO));
			mEtBody.setText(bundle.getString(EXTRA_BODY));
		}

		if (!TextUtils.isEmpty(mEtBody.getText().toString())) {
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					String msg = "";
					msg = getString(R.string.recognize_ask_send_message);
					speakBeforeRecognize(msg, 0);
				}
			}, 1000);
		}

		mImgAttachment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(SendSmsEmailActivity.this, CameraActivity.class),
						RequestCode.REQUEST_CAMERA);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// check email config
		if (TextUtils.isEmpty(mSharePrefs.getGmailUsername())) {
			// show dialog to confirm setup email
			mDialog.showConfirmDialog(R.string.dialog_confirm_setup_email, new OnConfirmClickListener() {

				@Override
				public void onConfirmOkClick() {
					startActivity(new Intent(SendSmsEmailActivity.this, EmailSetupActivity.class));
				}

				@Override
				public void onConfirmCancelClick() {
					showCenterToast(R.string.msg_info_setup_email);
					finish();
				}
			});
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
			send();
		}
	}

	protected boolean validateEmail(String email) {
		boolean valid = false;
		if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) valid = true;
		return valid;
	}

	private void send() {
		if (TextUtils.isEmpty(mEtTo.getText())) return;
		List<Contact> list = mEtTo.getSelectedContact();
		String emailTo = "";
		SmsManager smsManager = SmsManager.getDefault();
		for (int i = 0; i < list.size(); i++) {
			Contact c = list.get(i);
			Log.d("zzz", c.name + " " + c.email + " " + c.mobile);
			if (mChkSms.isChecked()) {
				smsManager.sendTextMessage(c.mobile, null, mEtBody.getText().toString(), null, null);
			}
			if (mChkEmail.isChecked()) {
				emailTo += "," + c.email;
			}
		}
		if (mChkEmail.isChecked()) {
			if (!TextUtils.isEmpty(emailTo)) {
				emailTo = emailTo.substring(1);
			}
			new SendEmailTask(this, emailTo, "MOM-bot message", mEtBody.getText().toString(), image).execute();
		} else {
			showCenterToast(R.string.msg_info_send_email_success);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvSend:
				send();
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (RequestCode.REQUEST_CAMERA == requestCode && resultCode == Activity.RESULT_OK) {
			image = data.getStringExtra(EXTRA_IMAGE);
			if (!TextUtils.isEmpty(image)) {
				mImgAttachment.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage("file:/" + image, mImgAttachment);
			}
		}
	}
}
