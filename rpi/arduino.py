import serial


BAUD_RATE = 9600
SERIAL_PORT = '/dev/ttyACM0'


class ArduinoSerialCon:
	def __init__(self):
		self.serial_connection = None
		
	def listen(self):
		try:
			print ("Listening for serial connection")
			self.serial_connection = serial.Serial(self.serial_port, self.baud_rate, timeout=2)
			if self.serial_connection:
				print ("Establised connection to Arduino serial port")
		except Exception as e:
			print ("Unable to establish connection with Arduino || Error: %s"% e)
			return self.close()
			
	def close(self):
		if self.serial_connection:
			self.serial_connection.close()
			print("Connection to Arduino serial port closed")
		return "closed"
		
	def read(self):
		try:
			data = self.serial_connection.readline()
			print("[SER INFO] SER Recv %s " % str(data))
			return data
		except Exception as e:
			print ("Error receiving from Arduino || Error: %s" % e)
			return 
			
	def send(self,payload):
		try:
			payload = payload.rstrip().encode('utf-8')
			self.serial_connection.write(payload)
			print("Sent data to Arduino || Data: %s " % str(payload))
		except Exception as e:
			print("Error sending data to Arduino || Error: %s" % e)
			return self.close()
