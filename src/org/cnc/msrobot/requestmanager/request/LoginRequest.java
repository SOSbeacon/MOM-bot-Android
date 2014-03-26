package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.UserResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.SharePrefs;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request.Method;

public class LoginRequest extends RequestBase<UserResource> {

	public LoginRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(UserResource result) {
		if (result != null) {
			Log.d("MsRobot", "result.auth_token: " + result.auth_token);
			SharePrefs.getInstance().saveLoginToken(result.auth_token);
		}
	}

	@Override
	protected HashMap<String, String> addParams() {
		String start = getExtra().getString(Consts.PARAMS_USER_EMAIL);
		String end = getExtra().getString(Consts.PARAMS_USER_PASSWORD);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Consts.PARAMS_USER_EMAIL, start);
		params.put(Consts.PARAMS_USER_PASSWORD, end);
		return params;
	}

	@Override
	protected String buildRequestUrl() {
		return URLConsts.LOGIN_URL;
	}

	@Override
	protected Type getClassOf() {
		return UserResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.POST;
	}

}
