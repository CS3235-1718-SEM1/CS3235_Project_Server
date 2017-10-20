import pyotp
import serial
import base64

door_id="com1-1-rfid"
totp = pyotp.TOTP(base64.b32encode("crackthisrfid"))
lastPass = totp.now()
ser = serial.Serial('/dev/ttyS1', 115200)

while True:
    currPass = totp.now()
    if currPass != lastPass:
        print("currently showing :", door_id + "," + currPass)
        string = ""
        string = door_id+","+currPass+'\n'
        ser.write(string.encode())
    lastPass = currPass
