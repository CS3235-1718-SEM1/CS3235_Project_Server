from flask import Flask
from flask import request
from flask import send_from_directory

import pyotp

app = Flask(__name__, static_url_path='/static')

studentDir = {}
studentDir["student_matric"] = "studentSecretKey"
doorDir = {}
doorDir["com1-01-13"] = "doorSecretKey"

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
        return "Invalid Door id"
    if data['door_token'] == "" or data['door_token'] == None:
        return "Invalid Door Token" 
    if data['IVLE_id'] == "" or data['IVLE_id'] == None:
        return "Invalid IVLE id"
    if data['IVLE_token'] == "" or data['IVLE_token'] == None:
        return "Invalid IVLE token"

    if data["IVLE_id"] in studentDir and data["door_id"] in doorDir:
        stu_otp_token = studentDir[data["IVLE_id"]]
        door_otp_token = doorDir[data["door_id"]]
        student = pyotp.TOTP(stu_otp_token)
        door = pyotp.TOTP(door_otp_token)
        if student.now() == data["IVLE_token"]:
            if door.now() == data["door_token"]:
              openDoor(data["door_id"])
              return "Access Granted"
    return "Access Denied"

def openDoor(door):
    print("opening " + door)

if __name__ == '__main__':
    app.run(host= '0.0.0.0', port=5000)