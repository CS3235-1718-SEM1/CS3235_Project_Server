package com.cs3235.door.doorlockandroid.https;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HttpManager {
    private Context context;

    private RequestQueue httpRequestQueue;

    public HttpManager(Context context) {
        this.context = context;
        this.httpRequestQueue = Volley.newRequestQueue(context);
    }

    public <T> void sendNewHttpRequest(Request<T> request) {
        httpRequestQueue.add(request);
    }

    public String getDoorServerUrl() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("pref_doorServerUrl", "127.0.0.1:5000");
    }

    public String getSmartphoneServerUrl() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString("pref_smartphoneCardServerUrl", "127.0.0.1:6000");
    }
}
