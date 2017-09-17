import pyotp
import oledQrCodeGenerator

totp = pyotp.TOTP('base32secret3232')
lastPass = totp.now()

while True:
    currPass = totp.now()
    if currPass != lastPass:
        print("currently showing :", currPass)
        oledQrCodeGenerator.dispQrCode(currPass)
    lastPass = currPass
