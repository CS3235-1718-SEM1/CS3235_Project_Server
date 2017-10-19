package com.cs3235.door.doorlockandroid.door;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.https.UnlockDoorRequest;
import com.cs3235.door.doorlockandroid.login.User;

import java.util.HashMap;
import java.util.Map;

public class DoorUnlocker {
    private static final String ACCESS_GRANTED_MESSAGE = "Access Granted";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";

    private HttpManager httpManager;

    private DoorUnlockResultCallback doorStatusUpdateCallback;

    public DoorUnlocker(HttpManager httpManager, DoorUnlockResultCallback resultCallback) {
        this.httpManager = httpManager;
        this.doorStatusUpdateCallback = resultCallback;
    }

    // TODO: Refactor: This needs to be async!
    public void unlockDoor(ScannedDoorDetails doorToUnlock, User requester) {
        final String doorId = doorToUnlock.id;

        if (requester == null) {
            doorStatusUpdateCallback.doorUnlockStatusUpdated("No user logged in!");
            return;
        }

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("door_id", doorToUnlock.id);
        requestParams.put("door_token", doorToUnlock.otpToken);
        requestParams.put("IVLE_id", requester.ivleId);
        requestParams.put("IVLE_token", requester.getUserOtp());

        HttpManager.RequestResult<String> requestResult = httpManager.sendNewStringRequest(
                Request.Method.POST,
                httpManager.getDoorServerUrl() + "/openDoor",
                requestParams,
                HttpManager.DEFAULT_TIMEOUT,
                HttpManager.DEFAULT_RETRY_INTERVAL);

        String newStatus = "";
        switch (requestResult.getResponse()) {
            case ACCESS_GRANTED_MESSAGE:
                newStatus = "Welcome to " + doorId;
                break;

            case ACCESS_DENIED_MESSAGE:
                newStatus = "USER DOES NOT HAVE ACCESS TO DOOR";
                break;

            default:
                newStatus = "Fail to open door: " + requestResult.getResponse();
                break;
        }

        doorStatusUpdateCallback.doorUnlockStatusUpdated(newStatus);
    }
}
