package org.cnc.msrobot.requestmanager;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.apache.http.client.utils.URLEncodedUtils;
import org.cnc.msrobot.utils.Logger;

import android.content.Context;
import android.os.Bundle;

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

	protected HashMap<String, String> addParams() {
		return null;
	}

	protected HashMap<String, File> addUploadFile() {
		return null;
	}

	public void execute(int action, Listener<T> listener, ErrorListener errorListener) {
		String url = buildRequestUrl();
		Logger.debug(TAG, "request url: " + url);
		int method = getMethod();
		GsonRequest<T> request = new GsonRequest<T>(method, url, getClassOf(), this, listener, errorListener);
		HashMap<String, String> params = addParams();
		if (params != null) {
			for (String key : params.keySet()) {
				request.addStringUpload(key, params.get(key));
			}
			request.setHeader("Content-Type", URLEncodedUtils.CONTENT_TYPE);
		}
		HashMap<String, File> uploadFile = addUploadFile();
		if (uploadFile != null) {
			for (String key : uploadFile.keySet()) {
				request.addFileUpload(key, uploadFile.get(key));
			}
			request.setHeader("Content-Type", URLEncodedUtils.CONTENT_TYPE);
		}
		request.setTag(action);
		mRequestManager.getRequestQueue().add(request);
	}
}