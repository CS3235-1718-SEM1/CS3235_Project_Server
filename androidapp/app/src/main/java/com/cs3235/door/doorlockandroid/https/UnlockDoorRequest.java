package com.cs3235.door.doorlockandroid.https;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.cs3235.door.doorlockandroid.door.ScannedDoorDetails;

import java.util.HashMap;
import java.util.Map;

public class UnlockDoorRequest extends StringRequest {
    public static final String UNLOCK_DOOR_URL = "/openDoor";

    private final ScannedDoorDetails doorToUnlock;
    private final String currentUser;
    private final String userSecretKey;

    public UnlockDoorRequest(String doorServerUrl, ScannedDoorDetails doorToUnlock,
                             String currentUser, String userSecretKey,
                             Response.Listener<String> listener,
                             Response.ErrorListener errorListener) {
        super(Request.Method.POST, doorServerUrl + UNLOCK_DOOR_URL, listener, errorListener);

        this.doorToUnlock = doorToUnlock;
        this.currentUser = currentUser;
        this.userSecretKey = userSecretKey;
    }

    @Override
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("door_id", doorToUnlock.id);
        params.put("door_token", doorToUnlock.otpToken);
        params.put("ivle_id", currentUser);
        params.put("ivle_token", userSecretKey);

        return params;
    }
}
