package org.cnc.mombot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.Date;

import org.cnc.mombot.provider.DbContract.TableEvent;
import org.cnc.mombot.requestmanager.RequestBase;
import org.cnc.mombot.resource.EventResource;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.DateTimeFormater;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.URLConsts;

import android.content.ContentValues;
import android.content.Context;

import com.android.volley.Request.Method;

public class GetListEventRequest extends RequestBase<EventResource[]> {
	private Date start, end;

	public GetListEventRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(EventResource[] result) {
		if (getExtra().getBoolean(Consts.PARAMS_QUERY_NOT_SAVE_DB)) return;
		String where = TableEvent.START + ">=datetime('" + DateTimeFormater.compareFormater.format(start) + "') AND "
				+ TableEvent.START + "<=datetime('" + DateTimeFormater.compareFormater.format(end) + "')";
		Logger.debug("GetListEventRequest", "where: " + where);
		// delete all event in range
		int rowDelete = mContext.getContentResolver().delete(TableEvent.CONTENT_URI, where, null);
		Logger.debug("GetListEventRequest", "row deleted: " + rowDelete);
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
		start = new Date(getExtra().getLong(Consts.PARAMS_QUERY_START));
		end = new Date(getExtra().getLong(Consts.PARAMS_QUERY_END));
		String token = SharePrefs.getInstance().getLoginToken();
		String url = URLConsts.GET_LIST_EVENT_URL
				.replace(Consts.HOLDER_QUERY_START, DateTimeFormater.timeServerFormat.format(start))
				.replace(Consts.HOLDER_QUERY_END, DateTimeFormater.timeServerFormat.format(end))
				.replace(Consts.HOLDER_AUTH_TOKEN, token);
		Logger.debug("GetListEventRequest", url);
		return url;
	}

	@Override
	protected Type getClassOf() {
		return EventResource[].class;
	}

	@Override
	protected int getMethod() {
		return Method.GET;
	}

}
