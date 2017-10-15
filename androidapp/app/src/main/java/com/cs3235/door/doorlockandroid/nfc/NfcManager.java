package com.cs3235.door.doorlockandroid.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import com.cs3235.door.doorlockandroid.door.ScannedDoorDetails;

public class NfcManager {
    /**
     * Handles an intent that is of {@link NfcAdapter#ACTION_NDEF_DISCOVERED}
     */
    public static ScannedDoorDetails handleNfcNdefDiscoveredIntent(Intent intent) {
        // TODO: Probably invent a better protocol?
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages != null && rawMessages.length >= 1) {
            // just check the first packet
            NdefMessage ndefMessage = (NdefMessage)rawMessages[0];

            if (ndefMessage.getRecords().length >= 1) {
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];

                return ScannedDoorDetails.createDoorDetailsFromNfc(ndefRecord.toUri().toString());
            }
        }

        return null;
    }
}
