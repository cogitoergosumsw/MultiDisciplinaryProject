# Bi-directional communication between RPi and PC
# RPi as the server, PC as the client

IP_ADDRESS = "192.168.18.20"  # IP Address of PC
IP_PORT = 12316  # change accordingly if IP port not closed

import socket


class TCPConnection:

    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock2 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.tcp_server_wait(5, IP_PORT)
        self.tcp_server_next()

    def tcp_connect(self, host_ip, host_port):
        try:
            self.sock.connect((host_ip, host_port))
        except Exception as e:
            print(
                "TCP Connection error, unable to establish TCP Connection with PC || IP Address: {0} || IP Port: {1}".format(
                    host_ip, host_port))
        return "TCP Connection Established: %s", self.sock

    def tcp_server_wait(self, num_of_client_wait, ip_port):
        self.sock2.bind(('', ip_port))
        self.sock2.listen(num_of_client_wait)

    def tcp_server_next(self):
        client_socket, address = self.sock2.accept()
        self.sock = client_socket

    def tcp_write(self, message):
        try:
            self.sock.send(message.encode())
            return "Message sent: %s" % message
        except Exception as e:
            print("Error sending data to PC || Error: %s" % e)

    def tcp_read(self):
        temp = ' '
        msg_read = ''
        while temp != '\r':
            temp = self.sock.recv(1024)
            temp = temp.decode()
            msg_read = msg_read + temp
            print("Message read from TCP connection: %s" % msg_read)
        return msg_read

    def tcp_close(self):
        self.sock.close()
        self.sock2.close()
        return "TCP Connection Closed"
