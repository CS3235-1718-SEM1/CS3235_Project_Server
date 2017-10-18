package com.cs3235.door.doorlockandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_IVLE_LOGIN_URL;

public class IvleLoginActivity extends AppCompatActivity {
    private static final String CALLBACK_URL = "http://localhost/";
    private static final String URL_PARAMETER_PREFIX = "&url=";
    private static final String TOKEN_PREFIX = "?token=";

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ivle_login);

        settingsManager = new SettingsManager(this);

        configureWebView();
    }

    /**
     * Configures the WebView to the startup state. NOTE: You should only call this once
     * at the start of the activity.
     */
    private void configureWebView() {
        WebView webView = (WebView)findViewById(R.id.ivle_web_view);
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
        // TODO: Implement this
    }
}
