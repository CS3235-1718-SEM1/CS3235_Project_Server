package com.cs3235.door.doorlockandroid.login;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.cs3235.door.doorlockandroid.https.HttpManager;

import java.util.HashMap;

public class IvleLoginManager {
    private final String LOGIN_STILL_LOADING = "Still loading...";

    private final HttpManager httpManager;

    // TODO: Replace with real authentication
    private final User fakeUser = new User("studentmatric", "studentIvleAuth");
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

    public void getUserWithAuthToken(final String ivleAuthToken,
                                                final IvleLoginResultCallback callback) {
        httpManager.sendNewStringRequestAsync(
                Request.Method.GET,
                httpManager.getIvleUserGetIdUrl() + "&Token=" + ivleAuthToken,
                new HashMap<String, String>(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(IvleLoginManager.class.toString(), "Ivle login received: " + response);
                        if (response.equals("\"\"")) {
                            callback.handleIvleUserIdFailure("Invalid auth token");
                        } else if (response.startsWith("\"") && response.endsWith("\"")) {
                            // id is surrounded with quotes, strip both of them off
                            String userId = response.substring(1, response.length() - 1);
                            Log.d(IvleLoginManager.class.toString(), "GOt userId " + userId);
                            callback.handleIvleUserIdSuccess(new User(userId, ivleAuthToken));
                        } else {
                            callback.handleIvleUserIdFailure("Invalid app token or server is down.");
                        }
                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String error) {
                        Log.d(IvleLoginManager.class.toString(), "Ivle login error received: " + error);
                        callback.handleIvleUserIdFailure(error);
                    }
                });
    }

    // TODO: Tidy this class up
    /**
     * Possible outcomes of {@link #loginToIvle(String, String)}.
     */
    public class IvleLoginResult {
        public boolean successful;
        public User user;
        public String failureMessage;

        /**
         * User successfully logged in.
         */
        IvleLoginResult(User user) {
            setSuccessful(user);
        }

        /**
         * There were some problems with logging in
         */
        IvleLoginResult(String errorMessage) {
            setFailure(errorMessage);
        }

        public void setSuccessful(User user) {
            this.successful = true;
            this.user = user;
            this.failureMessage = "";
        }

        public void setFailure(String errorMessage) {
            this.successful = false;
            this.user = null;
            this.failureMessage = errorMessage;
        }
    }
}
