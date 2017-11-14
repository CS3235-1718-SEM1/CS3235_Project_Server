package com.cs3235.door.doorlockandroid.https;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import org.json.JSONObject;

import java.util.Map;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_DOOR_SERVER_URL_KEY;
import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_IVLE_GET_ID_URL;
import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_SMARTPHONE_CARD_SERVER_URL_KEY;

public class HttpManager {
    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_RETRY_INTERVAL = 500;

    private static final String HTTP_RESPONSE_TIMEOUT = "Server took way too long to respond. Time out.";
    private static final String HTTP_RESPONSE_WAIT_CANCELLED = "The wait for response was cancelled.";

    private SettingsManager settingsManager;
    private RequestQueue httpRequestQueue;

    public HttpManager(Context context, SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.httpRequestQueue = Volley.newRequestQueue(context);
    }

    public void sendNewStringRequestAsync(int httpMethod, String url,
                                                           Map<String, String> params,
                                                           final Response.Listener<String> successCallback,
                                                           final Response.Listener<String> errorCallback) {
        // create a new http string request
        HttpStringRequest request = new HttpStringRequest(
                httpMethod,
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        successCallback.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorCallback.onResponse(error.getMessage());
                    }
                }
        );

        request.setRetryPolicy((new DefaultRetryPolicy(100000, 10,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

        // dispatch the request
        httpRequestQueue.add(request);



    }

    public void sendNewJsonRequestAsync(int httpMethod, String url,
                                                        JSONObject params,
                                                        final Response.Listener<JSONObject> successCallback,
                                                        final Response.Listener<String> errorCallback) {
        // create a new http json request
        JsonObjectRequest request = new JsonObjectRequest(
                httpMethod,
                url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        successCallback.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorCallback.onResponse(error.getMessage());
                    }
                }
        );

        request.setRetryPolicy((new DefaultRetryPolicy(100000, 10,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

        // dispatch the request
        httpRequestQueue.add(request);
    }

    public String getDoorServerUrl() {
        return settingsManager.getString(PREF_DOOR_SERVER_URL_KEY, "http://127.0.0.1:5000");
    }

    public String getSmartphoneServerUrl() {
        return settingsManager.getString(PREF_SMARTPHONE_CARD_SERVER_URL_KEY, "http://127.0.0.1:6000");
    }

    public String getIvleUserGetIdUrl() {
        return settingsManager.getString(PREF_IVLE_GET_ID_URL, "https://ivle.nus.edu.sg/api/Lapi.svc/UserID_Get?APIKey=<token>");
    }
}
