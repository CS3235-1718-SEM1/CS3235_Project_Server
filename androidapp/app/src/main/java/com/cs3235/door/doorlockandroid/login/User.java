package com.cs3235.door.doorlockandroid.login;

import android.content.Intent;

public class User {
    public final String ivleId;
    public final String ivleToken;

    public User(String ivleId, String ivleToken) {
        this.ivleId = ivleId;
        this.ivleToken = ivleToken;
    }

    public static User createFromLoginResultIntent(Intent loginIntentData) {
        return new User(
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_MATRIC),
                loginIntentData.getStringExtra(LoginResultIntentExtra.EXTRA_USER_SECRET_KEY));
    }
}
