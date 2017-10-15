package com.cs3235.door.doorlockandroid.login;

import com.cs3235.door.doorlockandroid.https.HttpManager;

public class IvleLoginManager {
    private final HttpManager httpManager;

    // TODO: Replace with real authentication
    private final User fakeUser = new User("studentmatric", "studentSecretKey");
    private final String fakeUserPassword = "password";

    public IvleLoginManager(HttpManager httpManager) {
        this.httpManager = httpManager;
    }

    public User loginToIvle(String userId, String password) {
        // TODO: Replace with real authentication
        try {
            // Simulate network access.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return null;
        }

        // TODO: Replace with real authentication
        if (userId.equals(fakeUser.ivleId) && password.equals(fakeUserPassword)) {
            return fakeUser;
        }

        return null;
    }
}
