from flask import Flask
import time
import onionGpio

rfid = onionGpio.OnionGpio(0)
qr = onionGpio.OnionGpio(1)


print("QR gpio mode :: " + qr.getDirection())
print("RFID gpio mode :: " + rfid.getDirection())

app = Flask(__name__)



@app.route("/rfidDoorOpen")
def openRFIDDoor():
        rfid.setValue(1)
        time.sleep(1)
        rfid.setValue(0)
        return "200"

@app.route("/qrDoorOpen")
def openQRDoor():
        qr.setValue(1)
        time.sleep(1)
        qr.setValue(0)
        return "200"

if __name__ == '__main__':
    app.run(host= '0.0.0.0', port=9999)