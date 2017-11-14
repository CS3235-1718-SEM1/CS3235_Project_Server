package com.cs3235.door.doorlockandroid.login;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
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

    public void loginToSmartphoneCardSystem(final User ivleUser, final SmartphoneCardCallback callback) {
        try {
            JSONObject params = new JSONObject();
            params.put("IVLE_id", ivleUser.ivleId);
            params.put("IVLE_auth", ivleUser.ivleAuth);

            httpManager.sendNewJsonRequestAsync(
                    Request.Method.POST,
                    httpManager.getSmartphoneServerUrl(),
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(SmartphoneCardLoginManager.class.toString(), "Smartphone received good response.");
                            if (response != null) {
                                Log.d(SmartphoneCardLoginManager.class.toString(), "SmartphoneCard received: " + response.toString());
                            } else {
                                Log.d(SmartphoneCardLoginManager.class.toString(), "SmartphoneCard received null");
                            }
                            try {
                                boolean jsonSuccess = response.getBoolean("success");

                                if (!jsonSuccess) {
                                    callback.handleRegisterUserFailure("User is not tied to phone.");
                                } else {
                                    String secretKey = response.getString("secret_key");
                                    callback.handleRegisterUserSuccess(new User(ivleUser.ivleId,
                                            ivleUser.ivleAuth, secretKey));
                                }
                            } catch (JSONException je) {
                                callback.handleRegisterUserFailure(je.getMessage());
                            }
                        }
                    },
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(SmartphoneCardLoginManager.class.toString(), "ERROR!");
                            if (response != null) {
                                Log.d(SmartphoneCardLoginManager.class.toString(), "Smartphone Server error " + response.toString());
                            } else {
                                Log.d(SmartphoneCardLoginManager.class.toString(), "SmartphoneCard Server error null");
                            }

                            callback.handleRegisterUserFailure(response);
                        }
                    }
            );
        } catch (JSONException je) {
            // TODO: Error handling
        }
    }
}
