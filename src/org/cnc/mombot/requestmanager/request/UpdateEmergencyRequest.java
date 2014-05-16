package org.cnc.mombot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.cnc.mombot.requestmanager.GsonRequest;
import org.cnc.mombot.requestmanager.RequestBase;
import org.cnc.mombot.resource.EmergencyResource;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.URLConsts;

import android.content.Context;

import com.android.volley.Request.Method;

public class UpdateEmergencyRequest extends RequestBase<EmergencyResource> {
    public static final String MIME_TYPE_AUDIO = "audio/m4a";

	public UpdateEmergencyRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(EmergencyResource result) {
		Logger.debug("UpdateEmergencyRequest", "success");
	}

	@Override
	protected void addParams(GsonRequest<EmergencyResource> request) {
		String audioUrl = getExtra().getString(Consts.PARAMS_MESSAGE_AUDIO);
		Logger.debug("UpdateEmergencyRequest", audioUrl);
		request.addParam(Consts.PARAMS_AUTH_TOKEN, SharePrefs.getInstance().getLoginToken());
		request.addParam(Consts.PARAMS_MESSAGE_AUDIO, audioUrl);
		super.addParams(request);
	}

	@Override
	protected HashMap<String, String> addUploadFile() {
		HashMap<String, String> fileInfo = new HashMap<String, String>();
		fileInfo.put(Consts.PARAMS_MESSAGE_AUDIO, MIME_TYPE_AUDIO);
		return fileInfo;
	}

	@Override
	protected String buildRequestUrl() {
		String id = getExtra().getString(Consts.PARAMS_ID);
		return URLConsts.UPDATE_EMERGENCY_URL.replace(Consts.HOLDER_ID_PARAM, id);
	}

	@Override
	protected Type getClassOf() {
		return EmergencyResource.class;
	}

	@Override
	protected int getMethod() {
		return Method.PUT;
	}

}
