import pyotp
import oledQrCodeGenerator

door_id="com1-1-13"
totp = pyotp.TOTP('base32secret3232')
lastPass = totp.now()

while True:
    currPass = totp.now()
    if currPass != lastPass:
        print("currently showing :", door_id + "," + currPass)
        oledQrCodeGenerator.dispQrCode( door_id + ',' + currPass)
    lastPass = currPass