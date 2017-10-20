from flask import Flask
from flask import request
from flask import send_from_directory
import base64
import requests
import pyotp
import simplejson as json

app = Flask(__name__, static_url_path='/static')

# studentDir = {}
# studentDir["studentmatric"] = base64.b32encode("studentmatric".encode())
# #studentDir["studentmatric"] = "studentmatric"
# print(studentDir["studentmatric"])
doorDir = {}
doorDir["com1-01-qr"] = base64.b32encode("base32secret3232".encode())
doorDir["com1-01-rfid"] = base64.b32encode("crackthisrfid".encode())

url = 'https://morning-springs-84372.herokuapp.com/can_access_door'
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}

@app.route("/")
def hello():
    return send_from_directory('static', 'index.html')

@app.route("/openDoor", methods=['Post'])
def openDoor():
    if request.method == 'POST':
        try:
            data = {}
            data['door_id'] = request.form.get('door_id')
            data['door_token'] = request.form.get('door_token')
            data['IVLE_id'] = request.form.get('IVLE_id')
            data['IVLE_token'] = request.form.get('IVLE_token')
            print(data)
            return validateRequest(data)
        except ValueError:
            print("Warning :: ValueError passing JSON dropping packet")
    return "Invalid Request"

def validateRequest(data):
    if data['door_id'] == "" or data['IVLE_id'] == None:
        return "Missing Door id"
    if data['door_token'] == "" or data['door_token'] == None:
        return "Missing Door Token" 
    if data['IVLE_id'] == "" or data['IVLE_id'] == None:
        return "Missing IVLE id"
    if data['IVLE_token'] == "" or data['IVLE_token'] == None:
        return "Missing IVLE token"

    if data["door_id"] in doorDir:
        door_otp_token = doorDir[data["door_id"]]
        door = pyotp.TOTP(door_otp_token)
        if door.now() == data["door_token"]:
            if queryServer(data):
                openDoor(data["door_id"])
                return "Access Granted"
    return "Access Denied "

def queryServer(data):
    try:
        package = {'door_id': data["door_id"], 'IVLE_id': data["IVLE_id"], 'otp': data['IVLE_token']}
        r = requests.post(url, data=json.dumps(package), headers=headers)
        print(r.status_code)
        if r.status_code is 200:
            return True
    except Exception:
        print("Unable to parse request form android")
    return False

def openDoor(door):
    if door == "com1-01-rfid":
        r = requests.get('http://192.168.43.128:9999/rfidDoorOpen')
    elif door == "com1-01-qr":
        r = requests.get('http://192.168.43.128:9999/qrDoorOpen')
    print("opening " + door)


# DEBUGGING CODE :: comment out when not in use
# @app.route("/rfid")
# def rfidOverride():
#     openDoor("com1-01-rfid")
#     return "200"
# @app.route("/qr")
# def qrOverride():
#     openDoor("com1-01-qr")
#     return "200"


if __name__ == '__main__':
    app.run(host= '0.0.0.0', port=5000)
