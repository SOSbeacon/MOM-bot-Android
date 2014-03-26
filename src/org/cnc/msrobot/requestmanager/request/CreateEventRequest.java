package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.SharePrefs;

import android.content.ContentValues;
import android.content.Context;

import com.android.volley.Request.Method;

public class CreateEventRequest extends RequestBase<EventResource[]> {

	public CreateEventRequest(Context context) {
		super(context);
	}

	@Override
	protected HashMap<String, String> addParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Consts.PARAMS_EVENT_CONTENT, getExtra().getString(Consts.PARAMS_EVENT_CONTENT));
		params.put(Consts.PARAMS_EVENT_END_DATE, getExtra().getString(Consts.PARAMS_EVENT_END_DATE));
		params.put(Consts.PARAMS_EVENT_END_TIME, getExtra().getString(Consts.PARAMS_EVENT_END_TIME));
		params.put(Consts.PARAMS_EVENT_REPEAT, getExtra().getString(Consts.PARAMS_EVENT_REPEAT));
		params.put(Consts.PARAMS_EVENT_START_TIME, getExtra().getString(Consts.PARAMS_EVENT_START_TIME));
		params.put(Consts.PARAMS_EVENT_TITLE, getExtra().getString(Consts.PARAMS_EVENT_TITLE));
		params.put(Consts.PARAMS_EVENT_TYPE, getExtra().getString(Consts.PARAMS_EVENT_TYPE));
		params.put(Consts.PARAMS_AUTH_TOKEN, SharePrefs.getInstance().getLoginToken());
		String[] dayOfWeeks = getExtra().getStringArray(Consts.PARAMS_EVENT_DAY_WEEK);
		if (dayOfWeeks != null) {
			for (String key : dayOfWeeks) {
				params.put(Consts.PARAMS_EVENT_DAY_WEEK, key);
			}
		}
		return params;
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
