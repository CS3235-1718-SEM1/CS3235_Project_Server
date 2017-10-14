package com.cs3235.door.doorlockandroid;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

import static com.cs3235.door.doorlockandroid.FingerprintActivity.RESULT_FINGERPRINT_NOT_RECOGNIZED;

public class MainActivity extends AppCompatActivity {

    private static int LOGIN_REQUEST_CODE = 0x000000001;
    private static int FINGER_PRINT_REQUEST_CODE = 0x000000002;

    private String currentUser = "";
    private String activatedDoorMessage = "";
    private String studentSecretKey = "";

    private String lastScannedQrCode = "";

    // for HTTP request posting
    private RequestQueue httpRequestQueue;

    // TODO: Actual webserver IP
    private static final String WEB_SERVER_URL = "127.0.0.1:5000";
    private static final String ACCESS_GRANTED_MESSAGE = "Access Granted";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";

    // TODO: Don't hardcode this to allow multiple door access
    private static final String DOOR_ID = "com1-01-13";

    class UnlockDoorRequest extends StringRequest {
        public static final String UNLOCK_DOOR_URL = WEB_SERVER_URL + "/openDoor";

        public UnlockDoorRequest(Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Request.Method.POST, UNLOCK_DOOR_URL, listener, errorListener);
        }

        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("door_id", DOOR_ID);
            params.put("door_token", lastScannedQrCode);
            params.put("ivle_id", currentUser);
            params.put("ivle_token", studentSecretKey);

            return params;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpRequestQueue = Volley.newRequestQueue(this);
    }

    private void unlockDoor() {
        UnlockDoorRequest request = new UnlockDoorRequest(
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals(ACCESS_GRANTED_MESSAGE)) {
                        activatedDoorMessage = "Welcome to " + lastScannedQrCode;
                    } else if (response.equals(ACCESS_DENIED_MESSAGE)) {
                        activatedDoorMessage = "USER DOES NOT HAVE ACCESS TO DOOR";
                    } else {
                        activatedDoorMessage = "Fail: Server sent unrecognized message. " + response;
                    }

                    updateMessageText();
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    activatedDoorMessage = "Fail to connect to server";
                    updateMessageText();
                }
            });

        httpRequestQueue.add(request);
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

                        lastScannedQrCode = ndefRecord.toUri().toString();
                        activateFingerprintActivity();
                    }
                }
            }
        }
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

                // now try and unlock the door
                unlockDoor();

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
