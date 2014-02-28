package org.cnc.msrobot.requestmanager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
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
public class GsonRequest<T> extends Request<T> {
	private static final String DATA_STRING = "\"data\":";
	/* To hold the parameter name and the File to upload */
	private Map<String, File> fileUploads = new HashMap<String, File>();

	/* To hold the parameter name and the string content to upload */
	private Map<String, String> stringUploads = new HashMap<String, String>();

	private Map<String, String> headers = new HashMap<String, String>();
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

	public void addFileUpload(String param, File file) {
		fileUploads.put(param, file);
	}

	public void addStringUpload(String param, String content) {
		stringUploads.put(param, content);
	}

	public Map<String, File> getFileUploads() {
		return fileUploads;
	}

	public Map<String, String> getStringUploads() {
		return stringUploads;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headers;
	}

	public void setHeader(String title, String content) {
		headers.put(title, content);
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