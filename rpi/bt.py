from bluetooth import *
class btconnection():
	def __init__(self):
		server_sock = BluetoothSocket(RFCOMM)
		server_sock.bind(("",PORT_ANY)) 
		server_sock.listen(1) 
		self.server_sock = server_sock
		self.port = server_sock.getsockname()[1] 
		uuid = "99999999-0000-0000-1111-eeeeeeeeeeee" 
		advertise_service( server_sock, "Group18-BluetoothService",           
		service_id = uuid,     
		service_classes = [ uuid, SERIAL_PORT_CLASS ],                    
		profiles = [ SERIAL_PORT_PROFILE ],  
			protocols = [ OBEX_UUID ])

	def establish_con(self):
		print("Waiting for connection on RFCOMM channel %d" % self.port)
		server_sock = self.server_sock
		try:
			client_sock, address = server_sock.accept()
			self.client_sock = client_sock
			return True
		except:
			print ("Error connecting to Bluetooth")
		print("Accepted connection from ", address)
		return False
	
	
	def send_msg(self,message):
		client_sock = self.client_sock
		client_sock.send(message)
	
	def listen_msg(self):
		while True:
			client_sock = self.client_sock
			data = client_sock.recv(2048)
			return data
			
	def disconnect(self):
		print ("Disconnected")
		self.client_sock.close()
		self.server_sock.close()

     
"""         
if __name__ == "__main__":
        bt = btconnection(); """
