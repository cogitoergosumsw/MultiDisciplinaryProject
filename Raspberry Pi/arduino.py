import serial
from threading import Thread
#BAUD_RATE = 9600
#SERIAL_PORT = '/dev/ttyACM0'

class ArduinoSerialCon():
    def __init__(self):
        self.serial_connection = None
        self.serial_port = '/dev/ttyACM0'
        self.baud_rate = 115200
        
    def establish_con(self):
        try:
            self.serial_connection = serial.Serial(self.serial_port, self.baud_rate, timeout=2)
        except Exception as e:
            return False
        return True

    def listen(self):
        try:
            print("Listening for serial connection")
            if self.serial_connection:
                print("Establised connection to Arduino serial port")
        except Exception as e:
            print("Unable to establish connection with Arduino || Error: %s" % e)
            return self.close()

    def close(self):
        if self.serial_connection:
            self.serial_connection.close()
            print("Connection to Arduino serial port closed")
        return "closed"

    def read(self):
        try:
            data = self.serial_connection.readline()
            return data
        except Exception as e:
            err_msg = "Error receiving from Arduino || Error: %s" % e
            print(err_msg)
            self.close()
            return err_msg

    def send(self, payload):
        try:
            payload = payload.rstrip().encode('utf-8')
            self.serial_connection.write(payload)
        except Exception as e:
            err_msg = "Error sending data to Arduino || Error: %s" % e
            print(err_msg)
            self.close()
            return err_msg
            
"""
if __name__ == "__main__":
        r = ArduinoSerialCon()
        r.establish_con()
        while True:
            data = r.read()
            print("Arduino Sensor data recieved was : %s AT " % (data))
"""


