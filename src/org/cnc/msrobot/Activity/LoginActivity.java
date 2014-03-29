package org.cnc.msrobot.activity;

import org.cnc.msrobot.MainApplication;
import org.cnc.msrobot.R;
import org.cnc.msrobot.resource.UserResource;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Logger;

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

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class LoginActivity extends BaseActivity {
	public static final int REQUEST_LOGIN_CODE = 1;
	public static final String EXTRA_EMAIL = "email";
	public static final String EXTRA_PASS = "pass";
	private Button btnSet;
	EditText edid, edpwd;
	String id, pwd;
	TabHost tabHost;
	Resources ressources;

	Listener<UserResource> mRequestLoginListener = new Listener<UserResource>() {

		@Override
		public void onResponse(UserResource response) {
			if (response != null && !TextUtils.isEmpty(response.auth_token)) {
				dismissProgress();
				// start reminder service
				MainApplication.alarm.setAlarmCheckServer(getApplicationContext());
				// start home activity
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			}
		}
	};

	ErrorListener mRequestError = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			dismissProgress();
			showCenterToast(R.string.err_login_fail);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		addListenerOnButton();
		edid = (EditText) findViewById(R.id.editId);
		edpwd = (EditText) findViewById(R.id.editPwd);

		String token = mSharePrefs.getLoginToken();
		Logger.debug("Login", "token: " + token);
		if (!TextUtils.isEmpty(token)) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}

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
				Bundle bundle = new Bundle();
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
				bundle.putString(Consts.PARAMS_USER_EMAIL, email);
				bundle.putString(Consts.PARAMS_USER_PASSWORD, pass);
				showProgress();
				mRequestManager.request(Actions.ACTION_LOGIN, bundle, mRequestLoginListener, mRequestError);
			}
		});
	}
}
