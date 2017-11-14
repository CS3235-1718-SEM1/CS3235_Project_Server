package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.login.IvleLoginManager;
import com.cs3235.door.doorlockandroid.login.IvleLoginResultCallback;
import com.cs3235.door.doorlockandroid.login.SmartphoneCardCallback;
import com.cs3235.door.doorlockandroid.login.SmartphoneCardLoginManager;
import com.cs3235.door.doorlockandroid.login.User;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import java.util.UUID;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_IVLE_LOGIN_URL;
import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_PHONE_UUID_KEY;

public class IvleLoginActivity extends AppCompatActivity implements IvleLoginResultCallback,
        SmartphoneCardCallback {
    private static final String CALLBACK_URL = "http://localhost/";
    private static final String URL_PARAMETER_PREFIX = "&url=";
    private static final String TOKEN_PREFIX = "?token=";

    private HttpManager httpManager;
    private SettingsManager settingsManager;

    private WebView webView;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ivle_login);

        settingsManager = new SettingsManager(this);
        httpManager = new HttpManager(getApplicationContext(), settingsManager);

        webView = (WebView)findViewById(R.id.ivle_web_view);
        configureWebView();

        progressView = findViewById(R.id.ivle_login_progress);
    }

    /**
     * Configures the WebView to the startup state. NOTE: You should only call this once
     * at the start of the activity.
     */
    private void configureWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view,
                                              WebResourceRequest request) {

                String currentUrl = request.getUrl().toString();
                String successUrlPrefix = CALLBACK_URL + TOKEN_PREFIX;

                if (currentUrl.startsWith(successUrlPrefix)) {
                    String authToken = currentUrl.substring(successUrlPrefix.length());
                    onIvleLoginSuccess(authToken);
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl(getLoginUrl());
    }

    private String getLoginUrl() {
        return settingsManager.getString(PREF_IVLE_LOGIN_URL, getString(R.string.ivle_login_default_url))
                + URL_PARAMETER_PREFIX + CALLBACK_URL;
    }

    private void onIvleLoginSuccess(String authToken) {
        IvleLoginManager ivleLoginManager = new IvleLoginManager(httpManager);
        ivleLoginManager.getUserWithAuthToken(authToken, this);
    }

    private String getPhoneUuid() {
        String uuid = settingsManager.getString(PREF_PHONE_UUID_KEY, "");

        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            settingsManager.setString(PREF_PHONE_UUID_KEY, uuid);
        }

        return uuid;
    }

    private void loginSuccess(User loggedInUser) {
        Log.d(IvleLoginActivity.class.toString(), "IVLE login success.");
        Intent result = loggedInUser.generateIntent();
        setResult(RESULT_OK, result);

        finish();
    }

    private void loginFail(String errorMessage) {
        if (errorMessage != null) {
            Log.d(IvleLoginActivity.class.toString(), "IVLE error: " + errorMessage.toString());
        } else {
            Log.d(IvleLoginActivity.class.toString(), "IVLE login error null");
        }

        Intent failure = new Intent();
        if (errorMessage == null) {
            failure.setData(Uri.parse("Error message is blank."));
        } else {
            failure.setData(Uri.parse(errorMessage));
        }
        setResult(RESULT_CANCELED, failure);

        finish();
    }

    @Override
    public void handleIvleUserIdSuccess(User user) {
        SmartphoneCardLoginManager smartphoneCardManager =
                new SmartphoneCardLoginManager(httpManager, getPhoneUuid());
        Log.d(this.getClass().getName(), "Contacting smartphone now.");
        smartphoneCardManager.loginToSmartphoneCardSystem(user, this);
    }

    @Override
    public void handleIvleUserIdFailure(String response) {
        loginFail(response);
    }

    @Override
    public void handleRegisterUserSuccess(User user) {
        loginSuccess(user);
    }

    @Override
    public void handleRegisterUserFailure(String response) {
        loginFail(response);
    }
}
