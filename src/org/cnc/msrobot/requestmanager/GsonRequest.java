package org.cnc.msrobot.requestmanager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Wrapper for Volley requests to facilitate parsing of json responses and MultiPart.
 * 
 * @author thanhlcm
 * 
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class GsonRequest<T> extends Request<T> {
	private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	private ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
	private boolean isUpload;
	private HashMap uploadItemInfo;
	/**
	 * Gson parser
	 */
	private final Gson mGson;

	private final Type mClassOf;

	/**
	 * Callback for response delivery
	 */
	private final Listener<T> mListener;

	/**
	 * Callback for request
	 */
	private final RequestListener<T> mRequestListener;

	/**
	 * @param method
	 *            Request type.. Method.GET etc
	 * @param url
	 *            path for the requests
	 * @param objectClass
	 *            expected class type for the response. Used by gson for serialization.
	 * @param requestListener
	 *            handler for request
	 * @param listener
	 *            handler for the response
	 * @param errorListener
	 *            handler for errors
	 */
	public GsonRequest(int method, String url, Type classOf, RequestListener<T> requestListener, Listener<T> listener,
			ErrorListener errorListener) {

		super(method, url, errorListener);
		this.mListener = listener;
		this.mRequestListener = requestListener;
		this.mClassOf = classOf;
		mGson = new Gson();

	}

	/**
	 * @return the uploadItemInfo
	 */
	public HashMap getUploadItemInfo() {
		return uploadItemInfo;
	}

	/**
	 * @param uploadItemInfo
	 *            the uploadItemInfo to set
	 */
	public void setUploadItemInfo(HashMap uploadItemInfo) {
		this.uploadItemInfo = uploadItemInfo;
	}

	/**
	 * @return the isUpload
	 */
	public boolean isUpload() {
		return isUpload;
	}

	/**
	 * @param isUpload
	 *            the isUpload to set
	 */
	public void setUpload(boolean isUpload) {
		this.isUpload = isUpload;
	}

	public void addParam(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	public void addHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public ArrayList<NameValuePair> getRequestParams() {
		return params;
	}

	public void setParams(ArrayList<NameValuePair> params) {
		this.params = params;
	}

	public ArrayList<NameValuePair> getRequestHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<NameValuePair> headers) {
		this.headers = headers;
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			T result = mGson.fromJson(json, mClassOf);
			mRequestListener.postAfterRequest(result);
			return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return Response.error(new ParseError(e));
		}
	}

	@Override
	protected void deliverResponse(T response) {
		if (mListener != null) {
			mListener.onResponse(response);
		}
	}
}