package org.cnc.msrobot.requestmanager.request;

import java.lang.reflect.Type;
import java.util.Date;

import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.requestmanager.RequestBase;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.DateTimeFormater;
import org.cnc.msrobot.utils.SharePrefs;

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
		String where = TableEvent.START + ">=datetime('" + DateTimeFormater.compareFormater.format(start) + "') AND "
				+ TableEvent.START + "<=datetime('" + DateTimeFormater.compareFormater.format(end) + "')";
		// delete all event in range
		mContext.getContentResolver().delete(TableEvent.CONTENT_URI, where, null);
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
