package com.cs3235.door.doorlockandroid.https;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.cs3235.door.doorlockandroid.door.ScannedDoorDetails;
import com.cs3235.door.doorlockandroid.login.User;

import java.util.HashMap;
import java.util.Map;

public class UnlockDoorRequest extends StringRequest {
    public static final String UNLOCK_DOOR_URL = "/openDoor";

    private final ScannedDoorDetails doorToUnlock;
    private final User requester;

    public UnlockDoorRequest(HttpManager httpManager,
                             ScannedDoorDetails doorToUnlock,
                             User requester,
                             Response.Listener<String> listener,
                             Response.ErrorListener errorListener) {
        super(Request.Method.POST, httpManager.getDoorServerUrl() + UNLOCK_DOOR_URL, listener, errorListener);

        this.doorToUnlock = doorToUnlock;
        this.requester = requester;
    }

    @Override
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("door_id", doorToUnlock.id);
        params.put("door_token", doorToUnlock.otpToken);
        params.put("IVLE_id", requester.ivleId);
        params.put("IVLE_token", requester.ivleToken);

        return params;
    }
}
