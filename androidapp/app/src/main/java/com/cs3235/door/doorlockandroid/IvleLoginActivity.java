package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.login.IvleLoginManager;
import com.cs3235.door.doorlockandroid.login.SmartphoneCardLoginManager;
import com.cs3235.door.doorlockandroid.login.User;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import java.util.UUID;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_IVLE_LOGIN_URL;
import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_PHONE_UUID_KEY;

public class IvleLoginActivity extends AppCompatActivity {
    private static final String CALLBACK_URL = "http://localhost/";
    private static final String URL_PARAMETER_PREFIX = "&url=";
    private static final String TOKEN_PREFIX = "?token=";

    private HttpManager httpManager;
    private SettingsManager settingsManager;

    private WebView webView;
    private View progressView;

    private UserLoginTask authTask = null;

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
        webView.loadUrl(getLoginUrl());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view,
                                              WebResourceRequest request) {

                String currentUrl = view.getUrl();
                String successUrlPrefix = CALLBACK_URL + TOKEN_PREFIX;

                if (currentUrl.startsWith(successUrlPrefix)) {
                    String authToken = currentUrl.substring(successUrlPrefix.length());
                    onIvleLoginSuccess(authToken);
                    return true;
                }

                return false;
            }
        });
    }

    private String getLoginUrl() {
        return settingsManager.getString(PREF_IVLE_LOGIN_URL, getString(R.string.ivle_login_default_url))
                + URL_PARAMETER_PREFIX + CALLBACK_URL;
    }

    private void onIvleLoginSuccess(String authToken) {
        if (authTask != null) {
            return;
        }

        showProgress(true);
        authTask = new UserLoginTask(authToken);
        authTask.execute((Void) null);
    }

    private void showProgress(boolean inProgress) {
        progressView.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        webView.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }

    private String getPhoneUuid() {
        String uuid = settingsManager.getString(PREF_PHONE_UUID_KEY, "");

        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            settingsManager.setString(PREF_PHONE_UUID_KEY, uuid);
        }

        return uuid;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String ivleAuthToken;

        private User loggedInUser;
        private String errorMessage;

        UserLoginTask(String ivleAuthToken) {
            this.ivleAuthToken = ivleAuthToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            IvleLoginManager ivleLoginManager = new IvleLoginManager(httpManager);
            IvleLoginManager.IvleLoginResult ivleResult = ivleLoginManager.getUserWithAuthToken(ivleAuthToken);

            if (!ivleResult.successful) {
                errorMessage = ivleResult.failureMessage;
                return false;
            }

            SmartphoneCardLoginManager smartphoneCardManager =
                    new SmartphoneCardLoginManager(httpManager, getPhoneUuid());
            SmartphoneCardLoginManager.SmartphoneCardLoginResult smartphoneDoorResult =
                    smartphoneCardManager.loginToSmartphoneCardSystem(ivleResult.user);

            if (!smartphoneDoorResult.successful) {
                errorMessage = smartphoneDoorResult.failureMessage;
                return false;
            }

            loggedInUser = smartphoneDoorResult.user;
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            showProgress(false);

            if (success) {
                Intent result = loggedInUser.generateIntent();
                setResult(RESULT_OK, result);

                finish();
            } else  {
                Intent failure = new Intent();
                failure.setData(Uri.parse(errorMessage));
                setResult(RESULT_CANCELED, failure);

                finish();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }
}
