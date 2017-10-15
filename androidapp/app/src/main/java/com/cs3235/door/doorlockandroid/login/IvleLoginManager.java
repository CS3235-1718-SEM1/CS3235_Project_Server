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

    public IvleLoginResult loginToIvle(String userId, String password) {
        // TODO: Replace with real authentication
        try {
            // Simulate network access.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return new IvleLoginResult("Failed to connect to server.");
        }

        // TODO: Replace with real authentication
        if (userId.equals(fakeUser.ivleId) && password.equals(fakeUserPassword)) {
            return new IvleLoginResult(fakeUser);
        }

        return new IvleLoginResult("Unrecognized user name or password");
    }

    /**
     * Possible outcomes of {@link #loginToIvle(String, String)}.
     */
    public class IvleLoginResult {
        public final boolean successful;
        public final User user;
        public final String failureMessage;

        /**
         * User successfully logged in.
         */
        IvleLoginResult(User user) {
            this.successful = true;
            this.user = user;
            this.failureMessage = "";
        }

        /**
         * There were some problems with logging in
         */
        IvleLoginResult(String errorMessage) {
            this.successful = false;
            this.user = null;
            this.failureMessage = errorMessage;
        }
    }
}
