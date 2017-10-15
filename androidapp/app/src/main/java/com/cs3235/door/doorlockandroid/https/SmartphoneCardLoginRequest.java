package com.cs3235.door.doorlockandroid.https;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.cs3235.door.doorlockandroid.login.User;

import java.util.HashMap;
import java.util.Map;

public class SmartphoneCardLoginRequest extends StringRequest {
    public static final String UNLOCK_DOOR_URL = "/validateSmartphone";

    private final User ivleUser;
    private final String phoneUuid;

    public SmartphoneCardLoginRequest(HttpManager httpManager,
                                      User ivleUser,
                                      String phoneUuid,
                                      Response.Listener<String> listener,
                                      Response.ErrorListener errorListener) {
        // TODO: Use the correct server
        super(Request.Method.POST, httpManager.getDoorServerUrl() + UNLOCK_DOOR_URL, listener, errorListener);

        this.ivleUser = ivleUser;
        this.phoneUuid = phoneUuid;
    }

    @Override
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("ivle_id", ivleUser.ivleId);
        params.put("ivle_token", ivleUser.ivleToken);
        params.put("uuid_id", phoneUuid);

        return params;
    }
}
