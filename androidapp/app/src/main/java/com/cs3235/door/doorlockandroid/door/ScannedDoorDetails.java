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
        return new ScannedDoorDetails(qrCodeContent, qrCodeContent);
    }

    public static ScannedDoorDetails createDoorDetailsFromNfc(String nfcContent) {
        // TODO: Actual parsing of the content
        return new ScannedDoorDetails(nfcContent, nfcContent);
    }
}
