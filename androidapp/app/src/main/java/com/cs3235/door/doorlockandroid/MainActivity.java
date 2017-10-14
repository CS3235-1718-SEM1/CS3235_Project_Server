package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cs3235.door.doorlockandroid.door.ScannedDoorDetails;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.https.UnlockDoorRequest;
import com.cs3235.door.doorlockandroid.login.User;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static com.cs3235.door.doorlockandroid.FingerprintActivity.RESULT_FINGERPRINT_NOT_RECOGNIZED;

public class MainActivity extends AppCompatActivity {

    private static int LOGIN_REQUEST_CODE = 0x000000001;
    private static int FINGER_PRINT_REQUEST_CODE = 0x000000002;

    private String activatedDoorMessage = "";

    private ScannedDoorDetails lastScannedDoor = null;
    private User loggedInUser = null;
    private HttpManager httpManager;

    // TODO: Actual webserver IP
    private static final String ACCESS_GRANTED_MESSAGE = "Access Granted";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpManager = new HttpManager(this);
    }

    private void unlockDoor() {
        UnlockDoorRequest request = new UnlockDoorRequest(
                httpManager,
                lastScannedDoor,
                loggedInUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals(ACCESS_GRANTED_MESSAGE)) {
                            activatedDoorMessage = "Welcome to " + lastScannedDoor.id;
                        } else if (response.equals(ACCESS_DENIED_MESSAGE)) {
                            activatedDoorMessage = "USER DOES NOT HAVE ACCESS TO DOOR";
                        } else {
                            activatedDoorMessage = "Fail: Server sent unrecognized message. " + response;
                        }

                        refreshMessage();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        activatedDoorMessage = "Fail to connect to server";
                        refreshMessage();
                    }
                }
        );

        Toast toast = Toast.makeText(getApplicationContext(), "Connecting to " + request.getUrl(), Toast.LENGTH_SHORT);
        toast.show();

        httpManager.sendNewHttpRequest(request);
    }

    public void onSettingsClick(View view) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // check whether the intent is resulted from an nfc scanning
        if (NfcAdapter.getDefaultAdapter(getApplicationContext()) != null) {
            if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

                Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (rawMessages != null && rawMessages.length >= 1) {
                    // just check the first packet
                    NdefMessage ndefMessage = (NdefMessage)rawMessages[0];

                    if (ndefMessage.getRecords().length >= 1) {
                        NdefRecord ndefRecord = ndefMessage.getRecords()[0];

                        lastScannedDoor = ScannedDoorDetails.createDoorDetailsFromNfc(ndefRecord.toUri().toString());
                        activateFingerprintActivity();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            handleQrScanActivityResult(requestCode, resultCode, data);
        } else if (requestCode == LOGIN_REQUEST_CODE) {
            handleLoginActivityResult(resultCode, data);
        } else if (requestCode == FINGER_PRINT_REQUEST_CODE) {
            handleFingerprintActivityResult(resultCode);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFingerprintActivityResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            unlockDoor();
        } else if (resultCode == RESULT_CANCELED) {
            spawnSnackbarMessage("Fingerprint scanning failed: Authentication cancelled");
        } else if (resultCode == RESULT_FINGERPRINT_NOT_RECOGNIZED) {
            spawnSnackbarMessage("Fingerprint does not belong to user!");
        }
    }

    private void handleLoginActivityResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            loggedInUser = User.createFromLoginResultIntent(data);
            refreshMessage();
        }
    }

    private void handleQrScanActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        boolean isScanningCancelled = (result == null || result.getContents() == null);

        if(isScanningCancelled) {
            spawnSnackbarMessage("QR Code scanning was cancelled");
            activatedDoorMessage = "";
        } else {
            // we scanned something
            lastScannedDoor = ScannedDoorDetails.createDoorDetailsFromQrCode(result.getContents());
            activateFingerprintActivity();
        }

        refreshMessage();
    }

    private void spawnSnackbarMessage(String text) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.mainActivity),
                text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void refreshMessage() {
        updateMessageText(constructMessageText());
    }

    private String constructMessageText() {
        String output = "";

        if (loggedInUser == null) {
            output = "Not logged in.";
        } else {
            output = "Logged in as " + loggedInUser.ivleId + ".";
        }

        if (!activatedDoorMessage.isEmpty()) {
            output += " " + activatedDoorMessage;
        }

        return output;
    }

    private void updateMessageText(String newMessageText) {
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText(newMessageText);
    }
}
