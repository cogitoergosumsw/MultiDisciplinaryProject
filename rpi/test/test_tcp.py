import socket, time

IP_ADDRESS = "192.168.18.18"
IP_PORT = 12318

class Test_TCPCon:

    def __init__(self):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.Tcp_connect(IP_ADDRESS, IP_PORT)

    def Tcp_connect(self, HostIp, Port):
        self.s.connect((HostIp, Port))
        return "Connected!"


    def Tcp_Write(self, D):
        D = D.encode()
        self.s.send(D)
        return


    def Tcp_Read(self):
        temp = ' '
        msg_read = ''
        while temp != '\r':
            temp = self.s.recv(1024)
            temp = temp.decode()
            msg_read = msg_read + temp
            print("msg received: %s" % msg_read)
        return msg_read

    def Tcp_Close(self):
        self.s.close()
        return


