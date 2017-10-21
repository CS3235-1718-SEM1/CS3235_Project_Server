package com.cs3235.door.doorlockandroid.login;

import com.android.volley.Request;
import com.cs3235.door.doorlockandroid.https.HttpManager;

import org.json.JSONException;
import org.json.JSONObject;

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

        try {
            JSONObject params = new JSONObject();
            params.put("IVLE_id", ivleUser.ivleId);
            params.put("IVLE_auth", ivleUser.ivleAuth);

            HttpManager.RequestResult<JSONObject> requestResult = httpManager.sendNewJsonRequest(
                    Request.Method.POST,
                    httpManager.getSmartphoneServerUrl() + "/registerUser",
                    params,
                    HttpManager.DEFAULT_TIMEOUT,
                    HttpManager.DEFAULT_RETRY_INTERVAL);

            SmartphoneCardLoginResult result = new SmartphoneCardLoginResult();

            if (!requestResult.isSuccessful()) {
                result.setFailure(requestResult.getFailureMessage());
            } else {
                boolean jsonSuccess = requestResult.getResponse().getBoolean("success");

                if (jsonSuccess) {
                    result.setFailure("User is not tied to phone.");
                } else {
                    String secretKey = requestResult.getResponse().getString("secret_key");
                    result.setSuccessful(new User(ivleUser.ivleId, ivleUser.ivleAuth, secretKey));
                }
            }

            return result;

        } catch (JSONException ex) {
            SmartphoneCardLoginResult errorResult = new SmartphoneCardLoginResult();
            errorResult.setFailure("JSONException: " + ex.getMessage());
            return errorResult;
        }
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
