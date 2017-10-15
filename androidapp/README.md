# Android app

## Implementation details

### Tweakable settings
These can be accessed and modified by tapping on "Settings" in the main menu:

* `pref_smartphoneCardServerUrl`: URL of the smartphone authentication server (e.g. "https://192.0.2.0:5000")
* `pref_doorServerUrl`: URL of the door unlocking server (e.g. "https://192.0.2.0:5000")

### Protocols used

#### Logging into IVLE

* (implementation details from Jung Kai)

#### Smartphone Authentication

Each phone is given a unique UUID. When registering his phone with the authority, the user can only log in from the phone with the given UUID. This prevents the user from logging in to multiple phones.

Smartphone Authentication is done immediately after the "Logging into IVLE" stage. Only by passing this stage can the user use his phone for unlocking doors.

* URL: `<pref_smartphoneCardServerUrl>/validateSmartphone`
* HTTP method: `POST`
* Parameters:
    * `IVLE_id`: IVLE id (matric number)
    * `IVLE_token`: Token generated from id
    * `uuid_id`: Phone user id
* Example:
    ```
    {
        'IVLE_id': 'e1234567'
        'IVLE_token': 'abc'
        'uuid_id': '123e4567-e89b-12d3-a456-426655440000'
    }
    ```
* Expected responses:
    * `Smartphone Valid <secret key>`: This phone is authentic (registered with the authority). The secret key is appended.
    * `Smartphone Invalid`: This phone is not permitted to unlock doors.

#### Scanning the Door (QR code)

There will be a QR code display installed outside the room, so that the user can just scan the door to notify the application which room the user wants to unlock. The user will be brought into the fingerprint stage next.

* QR code content: `<door_id>:<door_OTP>`
* Example:
    ```
    com1-b2-99:235425
    ```

#### Scanning the Door (NFC)

This is an alternative way of unlocking the door. There will be a NFC emitter installed outside the room, so that the user can bring his phone close to the door to notify the application which room the user wants to unlock (the design of the application is such that it will be launched if it hasn't when executing this action). The user will be brought into the fingerprint stage next.

* NFC type: `NDEF Formatted Tag`
* MIME type: `text/plain`
* Total messages expected: 1
* Total records expected: 1
* Record type: `URI`
* Content: `<door_id>:<door_OTP>`
* Example:
    ```
    com1-b2-99:235425
    ```

#### Fingerprint

The application will verify the fingerprint of the user, before proceeding to the next stage "Unlocking the Door".

#### Unlocking the Door

Upon scanning the door and verifying the fingerprint, a request will be sent to the door unlocking server to unlock the door.

* URL: `<pref_smartphoneCardServerUrl>/openDoor`
* HTTP method: `POST`
* Parameters:
    * `IVLE_id`: IVLE id (matric number)
    * `IVLE_token`: Token generated from id
    * `door_id`: Name of the door
    * `door_token`: Door's OTP
* Example:
    ```
    {
        'IVLE_id': 'e1234567'
        'IVLE_token': 'abc'
        'door_id': 'com1-b2-99'
        'door_token': '235425'
    }
    ```
* Expected responses:
    * `Access Granted`: The door is unlocked.
    * `Access Denied`: User either has the wrong credentials, does not have access to the door, or the OTP expired.
