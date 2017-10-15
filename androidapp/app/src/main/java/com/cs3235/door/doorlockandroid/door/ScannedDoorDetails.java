package com.cs3235.door.doorlockandroid.door;

public class ScannedDoorDetails {
    public final String id;
    public final String otpToken;

    public ScannedDoorDetails(String id, String otpToken) {
        this.id = id;
        this.otpToken = otpToken;
    }

    public static ScannedDoorDetails createDoorDetailsFromQrCode(String qrCodeContent) {
        // TODO: Actual parsing of the content
        String[] keyValue = qrCodeContent.split(":");

        if (keyValue.length < 2) {
            return new ScannedDoorDetails(qrCodeContent, qrCodeContent);
        }

        return new ScannedDoorDetails(keyValue[0], keyValue[1]);
    }

    public static ScannedDoorDetails createDoorDetailsFromNfc(String nfcContent) {
        // TODO: Actual parsing of the content
        String[] keyValue = nfcContent.split(":");

        if (keyValue.length < 2) {
            return new ScannedDoorDetails(nfcContent, nfcContent);
        }

        return new ScannedDoorDetails(keyValue[0], keyValue[1]);
    }
}
