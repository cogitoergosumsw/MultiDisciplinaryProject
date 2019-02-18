import socket, time

IP_ADDRESS = "192.168.18.18"
IP_PORT = 12316


def Tcp_connect(HostIp, Port):
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((HostIp, Port))
    return "Connected!"


def Tcp_Write(D):
    D = D.encode()
    s.send(D)
    return


def Tcp_Read():
    a = ' '
    b = ''
    while a != '\r' or a != '':
        a = s.recv(1024)
        a = a.decode()
        b = b + a
    return b


def Tcp_Close():
    s.close()
    return

Tcp_connect(IP_ADDRESS, IP_PORT)
while 1:
    print("Reading from PC || %s", Tcp_Read())

