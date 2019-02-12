# Bi-directional communication between RPi and PC

IP_ADDRESS = "192.168.18.18"
IP_PORT = 77

import socket, time


def Tcp_connect(HostIp, Port):
    global s
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((HostIp, Port))
    return


def Tcp_Write(D):
    s.send(D + '\r')
    return


def Tcp_Read():
    a = ' '
    b = ''
    while a != '\r':
        a = s.recv(1)
        b = b + a
    return b


def Tcp_Close():
    s.close()
    return


Tcp_connect(IP_ADDRESS, IP_PORT)
Tcp_Write('hi connected')
print
print(Tcp_Read())
Tcp_Write('hi from macbook')
print(Tcp_Read())
Tcp_Close()
