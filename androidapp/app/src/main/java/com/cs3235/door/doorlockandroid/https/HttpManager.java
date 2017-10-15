package com.cs3235.door.doorlockandroid.https;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_DOOR_SERVER_URL_KEY;
import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_SMARTPHONE_CARD_SERVER_URL_KEY;

public class HttpManager {
    private SettingsManager settingsManager;
    private RequestQueue httpRequestQueue;

    public HttpManager(Context context, SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.httpRequestQueue = Volley.newRequestQueue(context);
    }

    public <T> void sendNewHttpRequest(Request<T> request) {
        httpRequestQueue.add(request);
    }

    public String getDoorServerUrl() {
        return settingsManager.getString(PREF_DOOR_SERVER_URL_KEY, "127.0.0.1:5000");
    }

    public String getSmartphoneServerUrl() {
        return settingsManager.getString(PREF_SMARTPHONE_CARD_SERVER_URL_KEY, "127.0.0.1:6000");
    }
}
