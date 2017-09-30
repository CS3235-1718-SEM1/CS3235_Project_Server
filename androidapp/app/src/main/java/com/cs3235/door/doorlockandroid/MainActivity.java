package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static com.cs3235.door.doorlockandroid.FingerprintActivity.RESULT_FINGERPRINT_NOT_RECOGNIZED;

public class MainActivity extends AppCompatActivity {

    private static int LOGIN_REQUEST_CODE = 0x000000001;
    private static int FINGER_PRINT_REQUEST_CODE = 0x000000002;

    private String currentUser = "";
    private String activatedDoorMessage = "";
    private String studentSecretKey = "studentSecretKey";

    private String lastScannedQrCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLoginClick(View view) {
        Intent login = new Intent(this, LoginActivity.class);
        startActivityForResult(login, LOGIN_REQUEST_CODE);
    }

    public void activateFingerprintActivity() {
        Intent fingerprint = new Intent(this, FingerprintActivity.class);
        startActivityForResult(fingerprint, FINGER_PRINT_REQUEST_CODE);
    }

    public void onScanClick(View view) {
        new IntentIntegrator(this).initiateScan();
    }

    private void updateMessageText() {
        TextView textView = (TextView) findViewById(R.id.message);

        String textViewContent = "";

        if (currentUser.isEmpty()) {
            textViewContent = "Not logged in. ";
        }else {
            textViewContent = "Logged in as " + currentUser + ". ";
        }

        if (!activatedDoorMessage.isEmpty()) {
            textViewContent += activatedDoorMessage;
        }

        textView.setText(textViewContent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // activity result received is QR code
        if (requestCode == IntentIntegrator.REQUEST_CODE) {

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if(result == null || result.getContents() == null) {
                // the QR scanning is cancelled...
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainActivity),
                    "QR Code scanning was cancelled", Snackbar.LENGTH_SHORT);
                mySnackbar.show();

                activatedDoorMessage = "";
            } else {
                // we scanned something
                lastScannedQrCode = result.getContents();
                activateFingerprintActivity();
            }

            updateMessageText();
        }

        // activity result received is user's login name
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String[] loginDetails = data.getData().toString().split(":");

                currentUser = loginDetails[0];
                studentSecretKey = loginDetails[1];
                updateMessageText();
            }
        }

        // activity result received is fingerprint authentication
        if (requestCode == FINGER_PRINT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // should send HTTP request to activate door
                activatedDoorMessage = "Welcome to " + lastScannedQrCode;
                updateMessageText();

            } else if (resultCode == RESULT_CANCELED) {
                // the fingerprint scanning was abort by user
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainActivity),
                        "Fingerprint scanning failed: Authentication cancelled", Snackbar.LENGTH_SHORT);
                mySnackbar.show();

            } else if (resultCode == RESULT_FINGERPRINT_NOT_RECOGNIZED) {
                // this is not user's fingerprints!
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainActivity),
                        "Fingerprint does not belong to user!", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }
}
