package com.cs3235.door.doorlockandroid.login;

import com.android.volley.Request;
import com.cs3235.door.doorlockandroid.https.HttpManager;

import java.util.HashMap;
import java.util.Map;

public class SmartphoneCardLoginManager {
    private static final String SMARTPHONE_VALID_MESSAGE_PREFIX = "Smartphone Valid";
    private static final String SMARTPHONE_INVALID_MESSAGE = "Smartphone Invalid";

    private final HttpManager httpManager;
    private final String phoneUuid;

    public SmartphoneCardLoginManager(HttpManager httpManager, String phoneUuid) {
        this.httpManager = httpManager;
        this.phoneUuid = phoneUuid;
    }

    public SmartphoneCardLoginResult loginToSmartphoneCardSystem(final User ivleUser) {
        Map<String, String> params = new HashMap<>();
        params.put("IVLE_id", ivleUser.ivleId);
        params.put("IVLE_auth", ivleUser.ivleAuth);
        params.put("uuid_id", phoneUuid);

        HttpManager.RequestResult<String> requestResult = httpManager.sendNewStringRequest(
                Request.Method.POST,
                httpManager.getSmartphoneServerUrl() + "/validateSmartphone",
                params,
                HttpManager.DEFAULT_TIMEOUT,
                HttpManager.DEFAULT_RETRY_INTERVAL);

        SmartphoneCardLoginResult result = new SmartphoneCardLoginResult();

        if (!requestResult.isSuccessful()) {
            result.setFailure(requestResult.getFailureMessage());
        } else {
            String response = requestResult.getResponse();

            if (response.equals(SMARTPHONE_INVALID_MESSAGE)) {
                result.setFailure("User is not tied to phone.");
            } else if (response.startsWith(SMARTPHONE_VALID_MESSAGE_PREFIX)) {
                String secretKey = response.substring(SMARTPHONE_VALID_MESSAGE_PREFIX.length()).trim();
                result.setSuccessful(new User(ivleUser.ivleId, ivleUser.ivleAuth, secretKey));
            } else {
                result.setFailure("Smartphone server returns unknown message (error in protocol?).");
            }
        }

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
