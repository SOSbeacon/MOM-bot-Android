package org.cnc.msrobot.activity;

import org.cnc.mombot.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

public class EmailSetupActivity extends BaseActivity {
	public static final int REQUEST_LOGIN_CODE = 1;
	public static final String EXTRA_EMAIL = "email";
	public static final String EXTRA_PASS = "pass";
	private Button btnSet;
	EditText edid, edpwd;
	String id, pwd;
	TabHost tabHost;
	Resources ressources;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_email_login);
		addListenerOnButton();
		edid = (EditText) findViewById(R.id.editId);
		edpwd = (EditText) findViewById(R.id.editPwd);
		String user = mSharePrefs.getGmailUsername();
		String pass = mSharePrefs.getGmailPass();
		edid.setText(user);
		edpwd.setText(pass);
	}

	protected boolean validateEmail(String email) {
		boolean valid = false;
		if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) valid = true;
		return valid;
	}

	public void addListenerOnButton() {
		btnSet = (Button) findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				String email = edid.getText().toString().trim();
				String pass = edpwd.getText().toString().trim();
				if (!validateEmail(email)) {
					edid.setError(getString(R.string.err_email_invalid));
					edid.requestFocus();
					return;
				}
				if (TextUtils.isEmpty(pass)) {
					edpwd.setError(getString(R.string.err_password_invalid));
					edpwd.requestFocus();
					return;
				}
				Intent mail = new Intent();
				// save email & pass in SharePrefs
				mSharePrefs.saveGmailUsername(email);
				mSharePrefs.saveGmailPass(pass);
				// return result
				mail.putExtra(EXTRA_EMAIL, email);
				mail.putExtra(EXTRA_PASS, pass);
				setResult(Activity.RESULT_OK, mail);
				finish();
			}
		});
	}
}
