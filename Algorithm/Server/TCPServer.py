


#!/usr/bin/env python
import socket, time



class TCPServer:




	def __init__(self):
	
		self.Tcp_connect('192.168.18.18',77)
	


	#things to begin with
	def Tcp_connect( self, HostIp, Port ):
		global s
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		s.connect((HostIp, Port))
		return
		
	def Tcp_server_wait ( self,numofclientwait, port ):
		global s2
		s2 = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
		s2.bind(('',port)) 
		s2.listen(numofclientwait) 

	def Tcp_server_next (self ):
			global s
			s = s2.accept()[0]
	
	def Tcp_Write(self,D):
		s.send(D + '\r')
		return 
	
	def Tcp_Read(self ):
		a = ' '
		b = ''
		while a != '\r':
			a = s.recv(1)
			b = b + a
		return b

	def Tcp_Close( self):
		s.close()
		return 


if __name__ =="__main__":
	server = TCPServer()
	server.Tcp_server_wait ( 5, 17098 )
	server.Tcp_server_next()
	print(server.Tcp_Read())
	server.Tcp_Write('hi')
	print(server.Tcp_Read())
	server.Tcp_Write('hi')
	server.Tcp_Close()

