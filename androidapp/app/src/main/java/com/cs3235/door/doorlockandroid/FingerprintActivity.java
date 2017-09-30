package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class FingerprintActivity extends AppCompatActivity {

    public static final int RESULT_FINGERPRINT_NOT_RECOGNIZED = 3;

    FingerprintManager mFingerprintManager;
    CancellationSignal mFingerprintOperationCancellation;

    TextView mActivityMessage;

    FingerPrintCallback mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        mActivityMessage = (TextView) findViewById(R.id.fingerprintmessage);
        mCallbacks = new FingerPrintCallback();

        mFingerprintManager = getSystemService(FingerprintManager.class);
        mFingerprintOperationCancellation = new CancellationSignal();
        try {
            mFingerprintManager.authenticate(null, mFingerprintOperationCancellation, 0, mCallbacks, null);
        } catch (SecurityException se) {
            mActivityMessage.setText("Fingerprint sensors not available or permission not given.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFingerprintOperationCancellation.cancel();
    }

    class FingerPrintCallback extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationSucceeded (FingerprintManager.AuthenticationResult result) {
            mActivityMessage.setText("Authentication succeed");

            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onAuthenticationFailed() {
            mActivityMessage.setText("Fingerprint not recognized.");

            setResult(RESULT_FINGERPRINT_NOT_RECOGNIZED);
            finish();
        }

        @Override
        public void onAuthenticationError (int errorCode, CharSequence errString) {
            mActivityMessage.setText("Authentication failed. Try again. Error: " + errString);

            Intent activityResult = new Intent();
            setResult(RESULT_CANCELED, activityResult);
            finish();
        }
    }
}
