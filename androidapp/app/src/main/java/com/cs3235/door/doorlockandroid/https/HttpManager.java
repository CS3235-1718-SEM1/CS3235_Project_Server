package com.cs3235.door.doorlockandroid.https;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import org.json.JSONObject;

import java.util.Map;
import java.util.function.Consumer;

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

        // dispatch the request
        httpRequestQueue.add(request);
    }

    public RequestResult<String> sendNewStringRequest(int httpMethod, String url, Map<String, String> params,
                                      int timeOut, int retryInterval) {

        final RequestResult<String> requestResult = new RequestResult<>();

        // create a new http string request
        HttpStringRequest request = new HttpStringRequest(
                httpMethod,
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        synchronized (requestResult) {
                            requestResult.setSuccessful(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        synchronized (requestResult) {
                            requestResult.setFailure(error.getMessage());
                        }
                    }
                }
        );

        // dispatch the request
        httpRequestQueue.add(request);

        // wait until we either receive the request or timeout
        try {
            for (int i = 0; i < (timeOut / retryInterval); i++) {

                synchronized (requestResult) {
                    if (!requestResult.isStillLoading()) {
                        break;
                    }
                }

                Thread.sleep(retryInterval);
            }

            synchronized (requestResult) {
                if (!requestResult.isStillLoading()) {
                    requestResult.setFailure(HTTP_RESPONSE_TIMEOUT);
                }
            }

        } catch (InterruptedException e) {
            synchronized (requestResult) {
                requestResult.setFailure(HTTP_RESPONSE_WAIT_CANCELLED);
            }
        }

        return requestResult;
    }

    public RequestResult<JSONObject> sendNewJsonRequest(int httpMethod, String url,
                                                        JSONObject params,
                                                        int timeOut, int retryInterval) {

        final RequestResult<JSONObject> requestResult = new RequestResult<>();

        // create a new http json request
        JsonObjectRequest request = new JsonObjectRequest(
                httpMethod,
                url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        synchronized (requestResult) {
                            requestResult.setSuccessful(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        synchronized (requestResult) {
                            requestResult.setFailure(error.getMessage());
                        }
                    }
                }
        );

        // dispatch the request
        httpRequestQueue.add(request);

        // wait until we either receive the request or timeout
        try {
            for (int i = 0; i < (timeOut / retryInterval); i++) {

                synchronized (requestResult) {
                    if (!requestResult.isStillLoading()) {
                        break;
                    }
                }

                Thread.sleep(retryInterval);
            }

            synchronized (requestResult) {
                if (!requestResult.isStillLoading()) {
                    requestResult.setFailure(HTTP_RESPONSE_TIMEOUT);
                }
            }

        } catch (InterruptedException e) {
            synchronized (requestResult) {
                requestResult.setFailure(HTTP_RESPONSE_WAIT_CANCELLED);
            }
        }

        return requestResult;
    }

    public class RequestResult<T> {
        private boolean stillLoading;

        private boolean successful;

        private T response;
        private String failureMessage;

        public RequestResult() {
            stillLoading = true;
            successful = false;

            response = null;
            failureMessage = "";
        }

        public void setSuccessful(T content) {
            stillLoading = false;
            successful = true;

            response = content;
            failureMessage = "";
        }

        public void setFailure(String message) {
            stillLoading = false;
            successful = false;

            response = null;
            failureMessage = message;
        }

        public boolean isStillLoading() {
            return stillLoading;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public T getResponse() {
            return response;
        }

        public String getFailureMessage() {
            return getFailureMessage();
        }
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
