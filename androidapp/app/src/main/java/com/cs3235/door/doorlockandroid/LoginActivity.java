package com.cs3235.door.doorlockandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.login.IvleLoginManager;
import com.cs3235.door.doorlockandroid.login.LoginResultIntentExtra;
import com.cs3235.door.doorlockandroid.login.SmartphoneCardLoginManager;
import com.cs3235.door.doorlockandroid.login.User;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;

import java.util.UUID;

import static com.cs3235.door.doorlockandroid.settings.SettingsManager.PREF_PHONE_UUID_KEY;

// TODO: Delete this activity
/**
 * A login screen that offers login via email/password.
 *
 * @deprecated To be superseded by {@link IvleLoginActivity}
 */
@Deprecated
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}

