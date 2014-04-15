package org.cnc.msrobot.activity;

import java.util.List;

import org.cnc.msrobot.R;
import org.cnc.msrobot.module.AskYesNoModule;
import org.cnc.msrobot.module.AskYesNoModule.AskYesNoModuleListener;
import org.cnc.msrobot.resource.ContactResource;
import org.cnc.msrobot.resource.StaticResource;
import org.cnc.msrobot.task.SendEmailTask;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.ContactsEditText;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DialogUtils.OnConfirmClickListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class SendSmsEmailActivity extends BaseActivity implements OnClickListener, OnFocusChangeListener,
		AskYesNoModuleListener {
	public static final String EXTRA_CONTACT_POSITION = "extra_contact_position";
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
			int pos = bundle.getInt(EXTRA_CONTACT_POSITION);
			if (StaticResource.listContact != null && pos >= 0 && pos < StaticResource.listContact.size()) {
				ContactResource c = StaticResource.listContact.get(pos);
				mEtTo.addContact(c);
			}
			mEtBody.setText(bundle.getString(EXTRA_BODY));
		}

		if (!TextUtils.isEmpty(mEtBody.getText().toString())) {
			// run module ask yes no to send message
			new AskYesNoModule(this, input, output, R.string.recognize_ask_send_message, this).run();
		}

		// set click event
		findViewById(R.id.tvAttachment).setOnClickListener(this);

		// set click outsode for hide keyboard
		mEtBody.setOnFocusChangeListener(this);
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

	protected boolean validateEmail(String email) {
		boolean valid = false;
		if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) valid = true;
		return valid;
	}

	private void send() {
		if (TextUtils.isEmpty(mEtTo.getText())) return;
		List<ContactResource> list = mEtTo.getSelectedContact();
		String emailTo = "";
		SmsManager smsManager = SmsManager.getDefault();
		for (int i = 0; i < list.size(); i++) {
			ContactResource c = list.get(i);
			if (mChkSms.isChecked() && c.phone != null && !TextUtils.isEmpty(c.phone)) {
				try {
					smsManager.sendTextMessage(c.phone, null, mEtBody.getText().toString(), null, null);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
			case R.id.tvAction:
				send();
				break;
			case R.id.tvAttachment:
				startActivityForResult(new Intent(SendSmsEmailActivity.this, CameraActivity.class),
						RequestCode.REQUEST_CAMERA);
				break;
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

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (!hasFocus) {
			AppUtils.hideKeyboard(view);
		}
	}

	@Override
	public void onYes() {
		send();
	}

	@Override
	public void onNo() {
	}
}
