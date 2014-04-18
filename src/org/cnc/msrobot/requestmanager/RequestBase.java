package org.cnc.msrobot.requestmanager;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.apache.http.client.utils.URLEncodedUtils;
import org.cnc.msrobot.utils.Logger;
import org.cnc.msrobot.utils.VolleyHttpClient;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public abstract class RequestBase<T> implements RequestListener<T> {
	private static final String TAG = RequestBase.class.getSimpleName();
	private Bundle mBundle;
	protected Context mContext;
	private RequestManager mRequestManager = RequestManager.getInstance();

	public RequestBase(Context context) {
		mContext = context;
	}

	public void setExtra(Bundle bundle) {
		mBundle = bundle;
	}

	public Bundle getExtra() {
		return mBundle;
	}

	/**
	 * Subclasses must implement this to get url of request
	 * 
	 * @return url of request
	 */
	protected abstract String buildRequestUrl();

	protected abstract Type getClassOf();

	protected abstract int getMethod();

	protected void addParams(GsonRequest<T> request) {
	}

	/**
	 * return upload file info mimeType
	 * 
	 * @return
	 */
	protected HashMap<String, String> addUploadFile() {
		return null;
	}

	protected String setHeaderType() {
		return null;
	}

	public void execute(int action, Listener<T> listener, ErrorListener errorListener) {
		String url = buildRequestUrl();
		Logger.debug(TAG, "request url: " + url);
		int method = getMethod();
		GsonRequest<T> request = new GsonRequest<T>(method, url, getClassOf(), this, listener, errorListener);
		if (request != null) {
			request.setTag(action);
			if (request.isUpload()) {
				request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0,
						DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			}
			addParams(request);
			HashMap<String, String> files = addUploadFile();
			if (files != null) {
				request.setUpload(true);
				request.setUploadItemInfo(files);
			} else {
				request.addHeader(VolleyHttpClient.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);
			}
			request.addHeader("Cache-Control", "no-cache");
		}
		mRequestManager.getRequestQueue().add(request);
	}
}