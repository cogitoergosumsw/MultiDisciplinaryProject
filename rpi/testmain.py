from bt import btconnection
from c import Camera
from arduino import ArduinoSerialCon
from rpi_pc_conn import TCPConnection
from multiprocessing import Queue
from multiprocessing import Process
from collections import deque
from threading import Thread,current_thread
import time
import sys
import os

class mainthreading(Thread):
	def __init__(self):

		Thread.__init__(self) 
		
		print current_thread(), 'Main Thread'

		#set up bluetooth connection
		self.bt = btconnection()
		self.bt.establish_connection()

		#set up tcp connection
		self.tcp = TCPConnection()
		
		#list of messages to subsystems
		self.btlist = deque()
		self.arduinolist = deque()
		self.tcplist = deque()
		
		#place holder queue for listening threads
		self.placeholder = Queue(maxsize=0)
		
		#create queue for camera threads
		self.cameraaa = Queue(maxsize = 0)
		
		self.arduino = ArduinoSerialCon()
		self.arduino.listen()
		

		self.start_all_threads()
		
	def info(self,title):
		print title
		if hasattr(os, 'getppid'):  # only available on Unix
			print 'parent process:', os.getppid()
		print 'child process id:', os.getpid()
		
		
	def read_fromardunio(self):
		print (current_thread(), 'arduino read Thread')
		while True:
			data = self.arduino.read()
			if len(data>0):
				self.btlist.append(data)
				self.tcplist.append(data)
				print ("%s: Data from Arduino || Data: %s" % (time.ctime(), data))
        

	def write_toardunio(self):
		print (current_thread(), 'arduino write Thread')
		while True:
			if (len(self.arduinolist) > 0):
				message =  self.arduinolist.pop()
				print ("%s: Sending data to Arduino: %s" % (time.ctime(), message))

			if (self.placeholder.qsize() > 0):
				print "?"
            
	
	#listen to camera queue
	def listen_tocamera(self):
		print current_thread(), 'camera listen Thread'
		while (True):
			if not(self.cameraaa.empty()):
				try:
					message = self.cameraaa.get()
					self.btlist.append(message) #send message to bluetooth from camera
				except Exception as e:
					print ("error reading from camera %s"%e)

	
	
	#process to start camera, place queue 
	def startcamera(self):
		self.info('Camera process')
		self.camera = Camera(self.cameraaa)


				
	#thread to read from bluetooth	
	def read_frombluetooth(self):
		print current_thread(),  'BT read Thread'
		while (True):
			data = self.bt.listen_msg()
			try:
				if (len(data) > 0):
					print ("Data from bluetooth was: %s" %data) #if bluetooth recieves msg
					#TODO send to pc?
					self.arduinolist.append(data) #add to aurdino deque

			except Exception as e:
				print("Exception reading BT %s"%e)
				
		
	#thread to write to bluetooth 	 
	def write_tobluetooth(self):
		print current_thread(),  'BT write Thread'

		try:
			while (True):
				#insert data into bluetooth write queue here
				if (len(self.btlist) > 0 ): #if there is data from camera
					message = self.btlist.pop()
					self.bt.send_msg(message) #put it into the bluetooth queue
					print ("%s: Sending to Bluetooth || Data: %s" % (time.ctime(), message))
					
				if (self.placeholder.qsize() > 0):
					print "?"
					
		except Exception as e:
			print("Exception writing BT %s"%e)

	#thread to read data from TCP connection
	def read_fromtcpconnection(self):
		print(current_thread(), 'TCP Connection read thread')

		while(True):
			data = self.tcp.tcp_read()
			try:
				if (len(data) > 0):
					print("Data from TCP Connection: %s", data)
					self.arduinolist.append(data)
			except Exception as e:
				print("Exception reading data from TCP Connection || Error: %s", e)

	def write_totcpconnection(self):
		print(current_thread(), 'TCP Connection write thread')

		while(True):
			if (len(self.tcplist)>0): #if there is data for PC
				message = self.tcplist.pop()
				self.tcp_write(message) # send message to PC
				print("Sending message to PC || Data: %s")

		
	def start_all_threads(self):
		ttt = Thread(target=self.read_frombluetooth)
		tt = Thread(target=self.write_tobluetooth)
		
		c = Process(target=self.startcamera)
		c1 = Thread(target=self.listen_tocamera)
		
		ar = Thread(target=self.read_fromardunio)
		aw = Thread(target=self.write_toardunio)

		tcpr = Thread(target=self.read_fromtcpconnection)
		tcpw = Thread(target=self.write_totcpconnection)
		
		ttt.setDaemon(True)
		tt.setDaemon(True)
		c.daemon=True
		c1.setDaemon(True)
		ar.setDaemon(True)
		aw.setDaemon(True)
		tcpr.setDaemon(True)
		tcpw.setDaemon(True)
		
		c.start()
		c1.start()
		ttt.start()
		tt.start()

		ar.start()
		aw.start()

		tcpr.start()
		tcpw.start()
		
		c.join()
		c1.join()
		ttt.join()
		tt.join()

		ar.join()
		aw.join()
		tcpr.join()
		tcpw.join()

		
if __name__ == "__main__":
	try:
		m = Thread(target=mainthreading) #start main thread
		m.setDaemon(True)
		m.start()
		while True: time.sleep(1)
	except KeyboardInterrupt:
		print ("Main Thread killed")
		sys.exit()

