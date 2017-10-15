package com.cs3235.door.doorlockandroid.login;

import android.content.Intent;

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
}
