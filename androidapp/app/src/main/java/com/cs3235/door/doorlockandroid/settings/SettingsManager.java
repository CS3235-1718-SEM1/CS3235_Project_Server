package com.cs3235.door.doorlockandroid.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {
    public static final String PREF_DOOR_SERVER_URL_KEY = "pref_doorServerUrl";
    public static final String PREF_SMARTPHONE_CARD_SERVER_URL_KEY = "pref_smartphoneCardServerUrl";

    private final SharedPreferences sharedPref;

    public SettingsManager(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }
}
