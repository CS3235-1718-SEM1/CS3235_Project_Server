package com.cs3235.door.doorlockandroid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cs3235.door.doorlockandroid.door.DoorUnlockResultCallback;
import com.cs3235.door.doorlockandroid.door.DoorUnlocker;
import com.cs3235.door.doorlockandroid.door.ScannedDoorDetails;
import com.cs3235.door.doorlockandroid.https.HttpManager;
import com.cs3235.door.doorlockandroid.login.User;
import com.cs3235.door.doorlockandroid.nfc.NfcManager;
import com.cs3235.door.doorlockandroid.settings.SettingsManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static com.cs3235.door.doorlockandroid.FingerprintActivity.RESULT_FINGERPRINT_NOT_RECOGNIZED;

public class MainActivity extends AppCompatActivity implements DoorUnlockResultCallback {

    private static final String MESSAGE_NEED_LOGIN = "You must login first.";

    private static int LOGIN_REQUEST_CODE = 0x000000001;
    private static int FINGER_PRINT_REQUEST_CODE = 0x000000002;

    private String activatedDoorMessage = "";

    private ScannedDoorDetails lastScannedDoor = null;
    private User loggedInUser = null;
    private SettingsManager settingsManager;
    private HttpManager httpManager;
    private DoorUnlocker doorUnlocker;
    private NfcAdapter nfcAdapter;

    private BroadcastReceiver nfcAdapterStateChangedReceiver;
    private boolean nfcEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsManager = new SettingsManager(this);
        httpManager = new HttpManager(getApplicationContext(), settingsManager);
        doorUnlocker = new DoorUnlocker(httpManager, this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            nfcEnabled = false;
        } else {
            nfcEnabled = nfcAdapter.isEnabled();
        }

        loggedInUser = User.createFromSettings(settingsManager);
        // Log.d(this.getClass().getName(), loggedInUser.secretKey);

        nfcAdapterStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                    nfcEnabled = nfcAdapter.isEnabled();

                    if (nfcEnabled) enableNfcDetection();
                    else disableNfcDetection();

                    refreshMessage();
                }
            }
        };

        refreshMessage();
    }

    @Override
    protected void onResume() {
        enableNfcDetection();
        super.onResume();
        addNfcStatusReceiver();
    }

    @Override
    protected void onPause() {
        disableNfcDetection();
        super.onPause();
        removeNfcStatusReceiver();
    }

    public void onSettingsClick(View view) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    public void onLoginClick(View view) {
        Intent login = new Intent(this, IvleLoginActivity.class);
        startActivityForResult(login, LOGIN_REQUEST_CODE);
    }

    public void activateFingerprintActivity() {
        Intent fingerprint = new Intent(this, FingerprintActivity.class);
        startActivityForResult(fingerprint, FINGER_PRINT_REQUEST_CODE);
    }

    public void onScanClick(View view) {
        if (!isLoggedIn()) {
            spawnToastMessage(MESSAGE_NEED_LOGIN);
            return;
        }

        new IntentIntegrator(this).initiateScan();
    }

    @Override
    public void doorUnlockStatusUpdated(String newStatus) {
        activatedDoorMessage = newStatus;
        refreshMessage();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {

            if (NfcAdapter.getDefaultAdapter(getApplicationContext()) != null) {
                spawnToastMessage("Discovered NFC");
                if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                    lastScannedDoor = NfcManager.handleNfcNdefDiscoveredIntent(intent);

                    if (lastScannedDoor != null) {
                        if (!isLoggedIn()) {
                            spawnToastMessage(MESSAGE_NEED_LOGIN);
                            return;
                        }

                        activateFingerprintActivity();
                    } else {
                        spawnToastMessage("Unrecognized NFC tag!");
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
            doorUnlocker.unlockDoor(lastScannedDoor, loggedInUser);
            spawnToastMessage("Trying to unlock door...");
        } else if (resultCode == RESULT_CANCELED) {
            spawnSnackbarMessage("Fingerprint scanning failed: Authentication cancelled");
        } else if (resultCode == RESULT_FINGERPRINT_NOT_RECOGNIZED) {
            spawnSnackbarMessage("Fingerprint does not belong to user!");
        }
    }

    private void handleLoginActivityResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            loggedInUser = User.createFromLoginResultIntent(data);
            loggedInUser.saveToSettings(settingsManager);
        } else {
            if (data != null) {
                spawnSnackbarMessage(data.getDataString());
            } else {
                spawnSnackbarMessage("Login cancelled");
            }
        }

        refreshMessage();
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

    private void enableNfcDetection() {
        if (nfcAdapter == null) {
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableNfcDetection() {
        if (nfcAdapter == null) {
            return;
        }

        nfcAdapter.disableForegroundDispatch(this);
    }

    private void addNfcStatusReceiver() {
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(nfcAdapterStateChangedReceiver, filter);
    }

    private void removeNfcStatusReceiver() {
        this.unregisterReceiver(nfcAdapterStateChangedReceiver);
    }

    private void spawnToastMessage(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void spawnSnackbarMessage(String text) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.mainActivity),
                text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void refreshMessage() {
        updateMessageText(constructMessageText());
        updateNfcText();
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

    private void updateNfcText() {
        TextView textView = (TextView) findViewById(R.id.nfcstatus);
        textView.setText(nfcEnabled ? "Nfc is enabled" : "Nfc is DISABLED - Enable it to scan.");
    }

    private boolean isLoggedIn() {
        return loggedInUser != null;
    }
}
