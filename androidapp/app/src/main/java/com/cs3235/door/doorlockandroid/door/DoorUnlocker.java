package com.cs3235.door.doorlockandroid.door;

import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.https.UnlockDoorRequest;
import com.cs3235.door.doorlockandroid.login.User;

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

        UnlockDoorRequest request = new UnlockDoorRequest(
                httpManager,
                doorToUnlock,
                requester,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String newStatus = "";

                        switch (response) {
                            case ACCESS_GRANTED_MESSAGE:
                                newStatus = "Welcome to " + doorId;
                                break;

                            case ACCESS_DENIED_MESSAGE:
                                newStatus = "USER DOES NOT HAVE ACCESS TO DOOR";
                                break;

                            default:
                                newStatus = "Fail: Server sent unrecognized message. " + response;
                                break;
                        }

                        doorStatusUpdateCallback.doorUnlockStatusUpdated(newStatus);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String newStatus = "Fail to connect to server - " + error.getMessage();
                        doorStatusUpdateCallback.doorUnlockStatusUpdated(newStatus);
                    }
                }
        );

        httpManager.sendNewHttpRequest(request);
    }
}
