package org.cnc.msrobot.activity;

import org.cnc.msrobot.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

public class EmailSetupActivity extends Activity {
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

	}

	public void addListenerOnButton() {
		btnSet = (Button) findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mail = new Intent();
				mail.putExtra(EXTRA_EMAIL, edid.getText().toString());
				mail.putExtra(EXTRA_PASS, edpwd.getText().toString());
				setResult(Activity.RESULT_OK, mail);
				finish();
			}
		});
	}
}
