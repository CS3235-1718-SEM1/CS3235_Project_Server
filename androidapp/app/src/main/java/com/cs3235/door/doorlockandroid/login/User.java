package com.cs3235.door.doorlockandroid.login;

import android.content.Intent;

import com.cs3235.door.doorlockandroid.settings.SettingsManager;
import com.google.common.io.BaseEncoding;

import org.jboss.aerogear.security.otp.Totp;

public class User {
    public final String ivleId;
    public final String ivleAuth;
    public final String secretKey;

    public User(String ivleId, String ivleAuth) {
        this.ivleId = ivleId;
        this.ivleAuth = ivleAuth;
        this.secretKey = "";
    }

    public User(String ivleId, String ivleAuth, String secretKey) {
        this.ivleId = ivleId;
        this.ivleAuth = ivleAuth;
        this.secretKey = secretKey;
    }

    public String getUserOtp() {
        Totp totp = new Totp(getBase32EncodedSecretKey());
        return totp.now();
    }

    private String getBase32EncodedSecretKey() {
        return secretKey;
        //return BaseEncoding.base32().encode(secretKey.getBytes());
    }

    public static User createFromLoginResultIntent(Intent loginIntentData) {
        return new User(
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_MATRIC),
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_IVLE_AUTH),
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY));
    }

    /**
     * Create an intent containing the user information so that it can be transferred between
     * activities.
     */
    public Intent generateIntent() {
        Intent result = new Intent();

        result.putExtra(LoginResultIntentExtra.EXTRA_USER_MATRIC, ivleId);
        result.putExtra(LoginResultIntentExtra.EXTRA_USER_IVLE_AUTH, ivleAuth);
        result.putExtra(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY, secretKey);

        return result;
    }

    /**
     * Create a user by loading it from the settings.
     *
     * @param settingsManager to access the settings from.
     * @return null if settings does not exist/is invalid, a proper {@link User} if the settings
     * exist.
     */
    public static User createFromSettings(SettingsManager settingsManager) {
        String id = settingsManager.getString(LoginResultIntentExtra.EXTRA_USER_MATRIC, "");
        String auth = settingsManager.getString(LoginResultIntentExtra.EXTRA_USER_IVLE_AUTH, "");
        String secretKey = settingsManager.getString(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY, "");

        if (id.isEmpty() || auth.isEmpty() || secretKey.isEmpty()) {
            return null;
        }

        return new User(id, auth, secretKey);
    }

    /**
     * Save user details to the settings.
     *
     * @param settingsManager to access the settings from.
     */
    public void saveToSettings(SettingsManager settingsManager) {
        settingsManager.setString(LoginResultIntentExtra.EXTRA_USER_MATRIC, ivleId);
        settingsManager.setString(LoginResultIntentExtra.EXTRA_USER_IVLE_AUTH, ivleAuth);
        settingsManager.setString(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY, secretKey);
    }
}
