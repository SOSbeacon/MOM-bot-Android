package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.msrobot.requestmanager.GsonRequest;
import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.EmergencyResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.Logger;
import org.cnc.msrobot.utils.SharePrefs;

import android.content.Context;

import com.android.volley.Request.Method;

public class CreateEmergencyRequest extends RequestBase<EmergencyResource> {

	public CreateEmergencyRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(EmergencyResource result) {
		if (result != null) {
			Logger.debug("CreateEmergencyRequest", "id: " + result.id);
			SharePrefs.getInstance().saveEmergencyId(result.id);
		}
	}

	@Override
	protected void addParams(GsonRequest<EmergencyResource> request) {
		String lat = getExtra().getString(Consts.PARAMS_MESSAGE_LAT);
		String lon = getExtra().getString(Consts.PARAMS_MESSAGE_LON);
		request.addParam(Consts.PARAMS_AUTH_TOKEN, SharePrefs.getInstance().getLoginToken());
		request.addParam(Consts.PARAMS_MESSAGE_LAT, lat);
		request.addParam(Consts.PARAMS_MESSAGE_LON, lon);
		Logger.debug("CreateEmergencyRequest", "request params lon: " + lon + ", lat: " + lat + ", token: " + SharePrefs.getInstance().getLoginToken());
		super.addParams(request);
	}

	@Override
	protected String buildRequestUrl() {
		return URLConsts.CREATE_EMERGENCY_URL;
	}

	@Override
	protected Type getClassOf() {
		return EmergencyResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.POST;
	}

}
