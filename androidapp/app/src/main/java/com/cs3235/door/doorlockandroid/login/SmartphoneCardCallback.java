package com.cs3235.door.doorlockandroid.login;

public interface SmartphoneCardCallback {
    void handleRegisterUserSuccess(User user);
    void handleRegisterUserFailure(String response);
}
