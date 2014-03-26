package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.EmptyResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;

import android.content.Context;

import com.android.volley.Request.Method;

public class DeleteEventRequest extends RequestBase<EmptyResource> {

	public DeleteEventRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(EmptyResource result) {
	}

	@Override
	protected String buildRequestUrl() {
		String id = getExtra().getString(Consts.PARAMS_ID);
		return URLConsts.DELETE_EVENT_URL.replace(Consts.HOLDER_ID_PARAM, id);
	}

	@Override
	protected Type getClassOf() {
		return EmptyResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.POST;
	}

}
