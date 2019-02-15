# Bi-directional communication between RPi and PC

IP_ADDRESS = "192.168.18.18"        #IP Address of PC
IP_PORT = 77

import socket

class TCPConnection:

    def __init__(self):
        self.tcp_connect(IP_ADDRESS, IP_PORT)

    def tcp_connect(self, host_ip, host_port):
        global s
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((host_ip, host_port))
        except:
            print("TCP Connection error, unable to establish TCP Connection with PC || IP Address: %s || IP Port: %s", host_ip, host_port)
        return "TCP Connection Established: %s", s


    def tcp_write(self, message):
        try:
            s.send(message + '\r')
            return "Message sent: %s", message
        except Exception as e:
            print("Error sending data to PC || Error: %s", e)


    def tcp_read(self):
        temp = ' '
        msg_read = ''
        while temp != '\r':
            temp = s.recv(1024)
            msg_read = msg_read + temp
            print("Message read from TCP connection: %s", msg_read)
        return msg_read


    def tcp_close(self):
        s.close()
        return "TCP Connection Closed"

# for testing
    """
    if __name__ == "__main__":
        tcp_connect(IP_ADDRESS, IP_PORT)
        tcp_write('hi connected')
        print(tcp_read())
        tcp_write('hi from macbook')
        print(tcp_read())
        tcp_close()
    """