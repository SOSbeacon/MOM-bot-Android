package org.cnc.mombot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.cnc.mombot.provider.DbContract.TableEvent;
import org.cnc.mombot.requestmanager.GsonRequest;
import org.cnc.mombot.requestmanager.RequestBase;
import org.cnc.mombot.resource.EventResource;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.URLConsts;

import android.content.ContentValues;
import android.content.Context;

import com.android.volley.Request.Method;

public class CreateEventRequest extends RequestBase<EventResource[]> {

	public CreateEventRequest(Context context) {
		super(context);
	}

	@Override
	protected void addParams(GsonRequest<EventResource[]> request) {
		request.addParam(Consts.PARAMS_EVENT_CONTENT, getExtra().getString(Consts.PARAMS_EVENT_CONTENT));
		request.addParam(Consts.PARAMS_EVENT_END_DATE, getExtra().getString(Consts.PARAMS_EVENT_END_DATE));
		request.addParam(Consts.PARAMS_EVENT_END_TIME, getExtra().getString(Consts.PARAMS_EVENT_END_TIME));
		request.addParam(Consts.PARAMS_EVENT_REPEAT, getExtra().getString(Consts.PARAMS_EVENT_REPEAT));
		request.addParam(Consts.PARAMS_EVENT_START_TIME, getExtra().getString(Consts.PARAMS_EVENT_START_TIME));
		request.addParam(Consts.PARAMS_EVENT_TITLE, getExtra().getString(Consts.PARAMS_EVENT_TITLE));
		request.addParam(Consts.PARAMS_EVENT_TYPE, getExtra().getString(Consts.PARAMS_EVENT_TYPE));
		request.addParam(Consts.PARAMS_AUTH_TOKEN, SharePrefs.getInstance().getLoginToken());
		ArrayList<String> dayOfWeeks = getExtra().getStringArrayList(Consts.PARAMS_EVENT_DAY_WEEK);
		if (dayOfWeeks != null && dayOfWeeks.size() > 0) {
			for (String key : dayOfWeeks) {
				request.addParam(Consts.PARAMS_EVENT_DAY_WEEK, key);
				Logger.debug("CreateEventRequest", "day of week: " + key);
			}
		}
		Logger.debug("CreateEventRequest", "type: " + getExtra().getString(Consts.PARAMS_EVENT_TYPE));
		Logger.debug("CreateEventRequest", "day: " + getExtra().getString(Consts.PARAMS_EVENT_REPEAT));
		super.addParams(request);
	}

	@Override
	public void postAfterRequest(EventResource[] result) {
		// insert events
		if (result != null && result.length > 0) {
			ContentValues[] values = new ContentValues[result.length];
			for (int i = 0; i < result.length; i++) {
				if (result[i].start() != null && result[i].end() != null) {
					values[i] = result[i].prepareContentValue();
				}
			}
			mContext.getContentResolver().bulkInsert(TableEvent.CONTENT_URI, values);
		}
		mContext.getContentResolver().notifyChange(TableEvent.CONTENT_URI, null);
	}

	@Override
	protected String buildRequestUrl() {
		return URLConsts.CREATE_EVENT_URL;
	}

	@Override
	protected Type getClassOf() {
		return EventResource[].class;
	}

	@Override
	protected int getMethod() {
		return Method.POST;
	}

}
