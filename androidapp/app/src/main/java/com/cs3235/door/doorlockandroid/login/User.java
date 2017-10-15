package com.cs3235.door.doorlockandroid.login;

import android.content.Intent;

import org.jboss.aerogear.security.otp.Totp;

public class User {
    public final String ivleId;
    public final String ivleToken;
    public final String secretKey;

    public User(String ivleId, String ivleToken) {
        this.ivleId = ivleId;
        this.ivleToken = ivleToken;
        this.secretKey = "";
    }

    public User(String ivleId, String ivleToken, String secretKey) {
        this.ivleId = ivleId;
        this.ivleToken = ivleToken;
        this.secretKey = secretKey;
    }

    public static User createFromLoginResultIntent(Intent loginIntentData) {
        return new User(
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_MATRIC),
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_IVLE_TOKEN),
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY));
    }

    public String getUserOtp() {
        Totp totp = new Totp(secretKey);
        return totp.now();
    }

    /**
     * Create an intent containing the user information so that it can be transferred between
     * activities.
     */
    public Intent generateIntent() {
        Intent result = new Intent();

        result.putExtra(LoginResultIntentExtra.EXTRA_USER_MATRIC, ivleId);
        result.putExtra(LoginResultIntentExtra.EXTRA_USER_IVLE_TOKEN, ivleToken);
        result.putExtra(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY, secretKey);

        return result;
    }
}
