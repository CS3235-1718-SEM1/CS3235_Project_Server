package com.cs3235.door.doorlockandroid.login;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.https.IvleObtainIdRequest;

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

    // TODO: Refactor this, too similar to SmartphoneCardLoginManager's loginToSmartphoneCardSystem()
    public IvleLoginResult getUserWithAuthToken(final String ivleAuthToken) {
        final IvleLoginResult result = new IvleLoginResult(LOGIN_STILL_LOADING);

        // set up the request
        IvleObtainIdRequest request = new IvleObtainIdRequest(
                httpManager,
                ivleAuthToken,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals("\"\"")) {
                            synchronized (result) {
                                result.setFailure("Invalid auth token");
                            }
                        } else if (response.startsWith("\"") && response.endsWith("\"")) {
                            synchronized (result) {
                                // id is surrounded with quotes, strip both of them off
                                String userId = response.substring(1, response.length() - 1);

                                result.setSuccessful(new User(userId, ivleAuthToken));
                            }
                        } else {
                            synchronized (result) {
                                result.setFailure("Invalid app token or server is down.");
                            }
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        synchronized (result) {
                            result.setFailure("Fail to connect to server.");
                        }
                    }
                }
        );

        // dispatch the request
        httpManager.sendNewHttpRequest(request);

        // wait for http request to send, and set appropriate error message if the attempt failed
        try {
            int pollInterval = 500;
            int timeOut = 10000;
            for (int i = 0; i < (timeOut / pollInterval); i++) {
                Thread.sleep(pollInterval);

                synchronized (result) {
                    // TODO: Add a variable inside SmartphoneCardLoginResult to facilitate loading
                    // Otherwise we can only depend on SMARTPHONE_STILL_LOADING!
                    if (!result.failureMessage.equals(LOGIN_STILL_LOADING)) {
                        break;
                    }
                }
            }

            synchronized (result) {
                if (!result.successful && result.failureMessage.equals(LOGIN_STILL_LOADING)) {
                    result.setFailure("Server took way too long to response. Timed out.");
                }
            }

        } catch (InterruptedException e) {
            synchronized (result) {
                result.setFailure("Access to IVLE server was interrupted.");
            }
        }

        return result;
    }

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
