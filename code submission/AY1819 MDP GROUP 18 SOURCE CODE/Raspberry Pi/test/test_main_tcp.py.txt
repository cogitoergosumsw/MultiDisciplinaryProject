from test_tcp import Test_TCPCon
from collections import deque
from threading import Thread, current_thread
import time
import sys
import os


class mainthreading(Thread):
    def __init__(self):

        Thread.__init__(self)

        print(current_thread(), 'Main Thread')

        # set up tcp connection
        self.tcp = Test_TCPCon()

        self.tcplist = deque()

        self.start_all_threads()

    def info(self, title):
        print(title)
        if hasattr(os, 'getppid'):  # only available on Unix
            print('parent process:', os.getppid())
        print('child process id:', os.getpid())

    # thread to read data from TCP connection
    def read_fromtcpconnection(self):
        print(current_thread(), 'TCP Connection read thread')

        while (True):
            data = self.tcp.Tcp_Read()
            try:
                if data != '':
                    print("Data from TCP Connection: %s", data)
            except KeyboardInterrupt:
                if self.tcp:
                    self.tcp.close()
                    break
            except Exception as e:
                print("Exception reading data from TCP Connection || Error: %s" % e)
                if self.tcp:
                    self.tcp.close()
                    break
                break

        print(current_thread(), "TCP Connection read thread ended")

    def write_totcpconnection(self):
        print(current_thread(), 'TCP Connection write thread')

        while True:
            try:
                message = "hihi testing from pc"
                self.tcp.Tcp_Write(message)  # send message to PC
                time.sleep(5)
                print("Sending message to PC || Data: %s" %message)

            except KeyboardInterrupt:
                if self.tcp:
                    self.tcp.close()
                    break
            except Exception as e:
                print("Exception writing data through TCP Connection from RPi to PC || Error: %s" % e)
                break

    def start_all_threads(self):

        tcpr = Thread(target=self.read_fromtcpconnection)
        tcpw = Thread(target=self.write_totcpconnection)

        tcpr.setDaemon(True)
        tcpw.setDaemon(True)

        tcpr.start()
        tcpw.start()

        tcpr.join()
        tcpw.join()


if __name__ == "__main__":
    try:
        m = Thread(target=mainthreading)  # start main thread
        m.setDaemon(True)
        m.start()
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("Main Thread killed")
        sys.exit()
