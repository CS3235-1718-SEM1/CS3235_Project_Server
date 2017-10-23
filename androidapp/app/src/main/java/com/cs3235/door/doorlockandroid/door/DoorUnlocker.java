package com.cs3235.door.doorlockandroid.door;

import android.os.AsyncTask;
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

        httpManager.sendNewStringRequestAsync(
                Request.Method.POST,
                httpManager.getDoorServerUrl(),
                requestParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.startsWith(ACCESS_GRANTED_MESSAGE)) {
                            doorStatusUpdateCallback.doorUnlockStatusUpdated(
                                    "Welcome to " + doorId);
                        } else if (response.startsWith(ACCESS_DENIED_MESSAGE)) {
                            doorStatusUpdateCallback.doorUnlockStatusUpdated(
                                    "USER DOES NOT HAVE ACCESS TO DOOR");
                        } else {
                            doorStatusUpdateCallback.doorUnlockStatusUpdated(
                                    "Fail to open door: " + response);
                        }
                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String error) {
                        doorStatusUpdateCallback.doorUnlockStatusUpdated(error);
                    }
                });
    }

}
