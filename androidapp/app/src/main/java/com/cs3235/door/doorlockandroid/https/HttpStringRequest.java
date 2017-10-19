package com.cs3235.door.doorlockandroid.https;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class HttpStringRequest extends StringRequest {

    Map<String, String> params;

    /**
     *
     * @param httpMethod for example, POST will be {@link Request.Method#POST}
     * @param url
     * @param params
     * @param listener
     * @param errorListener
     */
    public HttpStringRequest(int httpMethod,
                             String url,
                             Map<String, String> params,
                             Response.Listener<String> listener,
                             Response.ErrorListener errorListener) {
        super(httpMethod, url, listener, errorListener);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
