package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.EmptyResource;
import org.cnc.msrobot.utils.Consts.URLConsts;

import android.content.Context;

import com.android.volley.Request.Method;

public class LogoutRequest extends RequestBase<EmptyResource> {

	public LogoutRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(EmptyResource result) {
	}

	@Override
	protected String buildRequestUrl() {
		return URLConsts.LOGOUT_URL;
	}

	@Override
	protected Type getClassOf() {
		return EmptyResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.DELETE;
	}

}
