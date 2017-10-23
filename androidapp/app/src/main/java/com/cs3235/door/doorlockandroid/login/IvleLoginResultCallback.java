package com.cs3235.door.doorlockandroid.login;

public interface IvleLoginResultCallback {
    void handleIvleUserIdSuccess(User user);
    void handleIvleUserIdFailure(String response);
}
