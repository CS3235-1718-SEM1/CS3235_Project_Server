package com.cs3235.door.doorlockandroid.login;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.https.SmartphoneCardLoginRequest;

public class SmartphoneCardLoginManager {
    private static final String SMARTPHONE_VALID_MESSAGE_PREFIX = "Smartphone Valid";
    private static final String SMARTPHONE_INVALID_MESSAGE = "Smartphone Invalid";
    private static final String SMARTPHONE_STILL_LOADING = "Still loading...";

    private final HttpManager httpManager;
    private final String phoneUuid;

    public SmartphoneCardLoginManager(HttpManager httpManager, String phoneUuid) {
        this.httpManager = httpManager;
        this.phoneUuid = phoneUuid;
    }

    public SmartphoneCardLoginResult loginToSmartphoneCardSystem(final User ivleUser) {
        final SmartphoneCardLoginResult result = new SmartphoneCardLoginResult();
        result.setFailure(SMARTPHONE_STILL_LOADING);

        // set up the request
        SmartphoneCardLoginRequest request = new SmartphoneCardLoginRequest(
                httpManager,
                ivleUser,
                phoneUuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals(SMARTPHONE_INVALID_MESSAGE)) {
                            synchronized (result) {
                                result.setFailure("User is not tied to phone.");
                            }
                        } else if (response.startsWith(SMARTPHONE_VALID_MESSAGE_PREFIX)) {
                            synchronized (result) {
                                String secretKey = response.substring(SMARTPHONE_VALID_MESSAGE_PREFIX.length());
                                result.setSuccessful(new User(ivleUser.ivleId, ivleUser.ivleToken, secretKey));
                            }
                        } else {
                            synchronized (result) {
                                result.setFailure("Smartphone server returns unknown message (error in protocol?).");
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
                    if (result.successful) {
                        break;
                    }
                }
            }

            synchronized (result) {
                if (!result.successful && result.failureMessage.equals(SMARTPHONE_STILL_LOADING)) {
                    result.setFailure("Server took way too long to response. Timed out.");
                }
            }

        } catch (InterruptedException e) {
            synchronized (result) {
                result.setFailure("Access to smartphone door server was interrupted.");
            }
        }

        // return the result
        return result;
    }

    /**
     * Possible outcomes of {@link #loginToSmartphoneCardSystem(User)}.
     */
    public class SmartphoneCardLoginResult {
        public boolean successful;
        public User user;
        public String failureMessage;

        /**
         * User successfully logged in.
         */
        public void setSuccessful(User user) {
            this.successful = true;
            this.user = user;
            this.failureMessage = "";
        }

        /**
         * There were some problems with logging in
         */
        public void setFailure(String errorMessage) {
            this.successful = false;
            this.user = null;
            this.failureMessage = errorMessage;
        }
    }
}
