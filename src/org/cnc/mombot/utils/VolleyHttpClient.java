package org.cnc.mombot.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.cnc.mombot.requestmanager.GsonRequest;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpStack;

/**
 * Custom implementation of com.android.volley.toolboox.HttpStack Uses apache HttpClient-4.2.5 jar to take care of . You
 * can download it from here http://hc.apache.org/downloads.cgi
 * 
 * @author Thanh Le
 * 
 */
public class VolleyHttpClient implements HttpStack {
    private static final String TAG = VolleyHttpClient.class.getSimpleName();
    private long totalSize = 1;
    public static final int DEFAULT_MAX_CONNECTIONS = 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    public final static String CONTENT_TYPE = "Content-Type";

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException,
        AuthFailureError {
        HttpUriRequest httpRequest = createHttpRequest(request);
        onPrepareRequest(httpRequest);
        return defaultClient.execute(httpRequest);
    }

    public static final HttpParams defaultHttpParams;
    static {
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setTimeout(params, DEFAULT_SOCKET_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(params, DEFAULT_MAX_CONNECTIONS);

        HttpConnectionParams.setConnectionTimeout(params, DEFAULT_SOCKET_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, DEFAULT_SOCKET_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Apache-HttpClient/Android");
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        // params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        defaultHttpParams = params;
    }

    public static final ThreadSafeClientConnManager defaultConnection;
    static {
        /* Make a thread safe connection manager for the client */
        final SSLSocketFactory socketFactory = MySSLSocketFactory.getFixedSocketFactory();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        /* Register schemes, HTTP and HTTPS */
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", socketFactory, 443));
        defaultConnection = new ThreadSafeClientConnManager(defaultHttpParams, schemeRegistry);
    }

    public static final HttpClient defaultClient;
    static {
        defaultClient = new DefaultHttpClient(defaultConnection, defaultHttpParams);
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     * 
     * @throws UnsupportedEncodingException
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /* protected */HttpUriRequest createHttpRequest(Request<?> request) throws AuthFailureError,
        UnsupportedEncodingException {

        if (request instanceof GsonRequest == false) {
            return null;
        }

        String url = ((GsonRequest) request).getUrl();
        ArrayList<NameValuePair> params = ((GsonRequest) request).getRequestParams();
        ArrayList<NameValuePair> headers = ((GsonRequest) request).getRequestHeaders();
        boolean isUpload = ((GsonRequest) request).isUpload();

        switch (request.getMethod()) {
        case Method.GET:
            // add parameters
            String combinedParams = "";
            if (!params.isEmpty()) {
                combinedParams += "?";
                for (NameValuePair p : params) {
                    String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                    if (combinedParams.length() > 1) {
                        combinedParams += "&" + paramString;
                    } else {
                        combinedParams += paramString;
                    }
                }
            }

            HttpGet getRequest = new HttpGet(url + combinedParams);

            // add headers
            for (NameValuePair h : headers) {
                getRequest.addHeader(h.getName(), h.getValue());
            }

            return getRequest;
        case Method.DELETE:
            HttpDelete deleteRequest = new HttpDelete(url);
            // add headers
            for (NameValuePair h : headers) {
                deleteRequest.addHeader(h.getName(), h.getValue());
            }
            return deleteRequest;
        case Method.POST: {
            HttpPost postRequest = new HttpPost(url);
            // add headers
            for (NameValuePair h : headers) {
                postRequest.addHeader(h.getName(), h.getValue());
            }

            if (!params.isEmpty()) {
                // if upload
                if (isUpload) {
                    postRequest.setEntity(buildUploadEntity(request));
                } else {
                    // if not upload
                    postRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }
            }
            return postRequest;
        }
        case Method.PUT: {
            HttpPut putRequest = new HttpPut(url);
            // add headers
            for (NameValuePair h : headers) {
                putRequest.addHeader(h.getName(), h.getValue());
            }
            if (!params.isEmpty()) {
                // if upload
                if (isUpload) {
                    putRequest.setEntity(buildUploadEntity(request));
                } else {
                    // if not upload
                    putRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }
            }
            return putRequest;
        }
        default:
            throw new IllegalStateException("Unknown request method.");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private MultipartEntity buildUploadEntity(Request<?> request) {
        HashMap uploadItemInfo = ((GsonRequest) request).getUploadItemInfo();
        ArrayList<NameValuePair> params = ((GsonRequest) request).getRequestParams();
        final MultipartEntity partEntity = new MultipartEntity();

        for (NameValuePair p : params) {
            try {
                // get mimeType in uploadFileInfoMap
                String mimeType = (String) uploadItemInfo.get(p.getName());
                if (mimeType != null && !"".equals(mimeType)) {
                    // if mimeType not null and not empty then add file to entity
                    partEntity.addPart(p.getName(), new FileBody(new File(p.getValue()), mimeType));
                } else {
                    // else add string to entity
                    partEntity.addPart(p.getName(),
                        new StringBody(p.getValue(), HTTP.PLAIN_TEXT_TYPE, Charset.forName(HTTP.UTF_8)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(TAG, e.getLocalizedMessage());
            }
        }
        totalSize = partEntity.getContentLength();
        return partEntity;
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     * 
     * <p>
     * Overwrite in subclasses to augment the request.
     * </p>
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }
}
