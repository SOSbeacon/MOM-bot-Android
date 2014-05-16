package org.cnc.mombot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.mombot.requestmanager.GsonRequest;
import org.cnc.mombot.requestmanager.RequestBase;
import org.cnc.mombot.resource.UserResource;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.URLConsts;

import android.content.Context;

import com.android.volley.Request.Method;

public class LoginRequest extends RequestBase<UserResource> {

	public LoginRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(UserResource result) {
		if (result != null) {
			Logger.debug("LoginRequest", "result.auth_token: " + result.auth_token);
			SharePrefs.getInstance().saveLoginToken(result.auth_token);
		}
	}

	@Override
	protected void addParams(GsonRequest<UserResource> request) {
		String start = getExtra().getString(Consts.PARAMS_USER_EMAIL);
		String end = getExtra().getString(Consts.PARAMS_USER_PASSWORD);
		request.addParam(Consts.PARAMS_USER_EMAIL, start);
		request.addParam(Consts.PARAMS_USER_PASSWORD, end);
		super.addParams(request);
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
