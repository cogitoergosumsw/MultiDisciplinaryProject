from bt import btconnection
from c import Camera
from arduino import ArduinoSerialCon
from rpi_pc_conn import TCPConnection
from multiprocessing import Queue
from multiprocessing import Process
from collections import deque
from threading import Thread, current_thread
import time
import sys
import os


class mainthreading(Thread):
    def __init__(self):
        Thread.__init__(self)
        print(current_thread(), 'Main Thread')
        # list of messages to subsystems
        self.btlist = deque()
        self.arduinolist = deque()
        self.tcplist = deque()
        # place holder queue for listening threads
        self.placeholder = Queue(maxsize=0)
        # create queue for camera threads
        self.cameraaa = Queue(maxsize=0)


        self.start_all_threads()

    def info(self, title):
        print(title)
        if hasattr(os, 'getppid'):  # only available on Unix
            print('parent process:', os.getppid())
        print('child process id:', os.getpid())

    def read_fromardunio(self):
        print(current_thread(), 'Arduino read Thread')
        while True:
            data = self.arduino.read()
            if (len(data) > 0):
                self.btlist.append(data)
                self.tcplist.append(data)
                print("%s: Data from Arduino || Data: %s" % (time.ctime(), data))

    def write_toardunio(self):
        print(current_thread(), 'Arduino write Thread')
        while True:
            if (len(self.arduinolist) > 0):
                message = self.arduinolist.pop()
                self.arduino.send(message)
                print("%s: Sending data to Arduino: %s" % (time.ctime(), message))

            if (self.placeholder.qsize() > 0):
                print("?")

    # listen to camera queue
    def listen_tocamera(self):
        print(current_thread(), 'camera listen Thread')
        while (True):
            if not (self.cameraaa.empty()):
                try:
                    message = self.cameraaa.get()
                    self.btlist.append(message)  # send message to bluetooth from camera
                except Exception as e:
                    print("error reading from camera %s" % e)

    # process to start camera, place queue
    def startcamera(self):
        self.info('Camera process')
        self.camera = Camera(self.cameraaa)

    # thread to read from bluetooth
    def read_frombluetooth(self):
        print(current_thread(), 'BT read Thread')
        while (True):
            data = self.bt.listen_msg()
            try:
                if (len(data) > 0):
                    print("Data from bluetooth was: %s" % data)  # if bluetooth recieves msg
                    """ TESTING SUBSYSTEM CON
                    """
                    
                    self.tcplist.append(data)
                    self.arduinolist.append(data)  # add to aurdino deque

            except Exception as e:
                print("Exception reading BT %s" % e)

    # thread to write to bluetooth
    def write_tobluetooth(self):
        print(current_thread(), 'BT write Thread')
        try:
            while (True):
                # insert data into bluetooth write queue here
                if (len(self.btlist) > 0):  # if there is data from camera
                    message = self.btlist.pop()
                    self.bt.send_msg(message)  # put it into the bluetooth queue
                    print("%s: Sending to Bluetooth || Data: %s" % (time.ctime(), message))

                if (self.placeholder.qsize() > 0):
                    print("?")

        except Exception as e:
            print("Exception writing BT %s" % e)


    # thread to read data from TCP connection
    def read_fromtcpconnection(self):
        print(current_thread(), 'TCP Connection read thread')
        while (True):
            data = self.tcp.tcp_read()
            try:
                if data != '':
                    print("Data from TCP Connection: %s", data)
                    self.arduinolist.append(data)
            except KeyboardInterrupt:
                if self.tcp:
                    self.tcp.close()
                    break
            except Exception as e:
                print("Exception reading data from TCP Connection || Error: %s" % e)
                break

        print(current_thread(), "TCP Connection read thread ended")



    #thread to write to TCP
    def write_totcpconnection(self):
        print(current_thread(), 'TCP Connection write thread')
        while True:
            try:
                if len(self.tcplist) > 0:  # if there is data for PC
                    message = self.tcplist.pop()
                    self.tcp.tcp_write(message)  # send message to PC
                    print("Sending message to PC || Data: %s" % message)

            except KeyboardInterrupt:
                if self.tcp:
                    self.tcp.close()
                    break
            except Exception as e:
                print("Exception writing data through TCP Connection from RPi to PC || Error: %s" % e)
                break
                
    def establish_bluetooth(self):
        print(current_thread(), 'establish_bluetooth')
        self.bt = btconnection()
        while (True):
            print 'WAITING FOR BLUETOOTH'
            if (self.bt.establish_con() == False):
                print '?'
            else:
                print 'BLUE TOOTH CONNECTED'
                break
            time.sleep(2)

    
    def establish_tcp(self):
        print(current_thread(), 'establish_tcp')
        self.tcp = TCPConnection()
        while (True):
            print 'WAITING FOR TCP'
            if (self.tcp.establish_con() == False):
                print '?'
            else:
                print 'TCP CONNECTED'
                break
            time.sleep(2)

    
    def establish_ard(self):
        print(current_thread(), 'establish_ard')
        self.arduino = ArduinoSerialCon()
        while (True):
            if (self.arduino.establish_con() == False):
                print 'WAITING FOR ARDUINO'

            else:
                print 'ARDUINO CONNECTED'
                break
            
            time.sleep(2)

        
        

    def start_all_threads(self):
        
        #SUBSYSTEM CONNECTION BLOCK
        establish_tcp_t = Thread(target=self.establish_tcp)
        establish_ard_t = Thread(target=self.establish_ard)
        establish_bluetooth_t = Thread(target=self.establish_bluetooth)
        c = Process(target=self.startcamera)
        
        #LISTENING THREADS
        listen_bluetooth_t = Thread(target=self.read_frombluetooth)
        listen_tcp_t = Thread(target=self.read_fromtcpconnection)
        listen_camera = Thread(target=self.listen_tocamera)
        listen_arduino_t = Thread(target=self.read_fromardunio)
        
        #WRITING THREADS
        write_bluetooth_t = Thread(target=self.write_tobluetooth)
        write_tcp_t = Thread(target=self.write_totcpconnection)
        write_ard_t = Thread(target=self.write_toardunio)
        
        #WRITING DAEMONS
        write_bluetooth_t.setDaemon(True)
        write_tcp_t.setDaemon(True)
        write_ard_t.setDaemon(True)
        
        
        #LISTENING DAEMONS
        listen_bluetooth_t.setDaemon(True)
        listen_tcp_t.setDaemon(True)
        listen_camera.setDaemon(True)
        listen_arduino_t.setDaemon(True)
    
        
        #SUBSYSTEM DAEMON THREAD
        establish_tcp_t.setDaemon(True)
        establish_ard_t.setDaemon(True)
        establish_bluetooth_t.setDaemon(True)
        c.daemon = True
        
        
        #SUBSYSTEM STARTING THREADS
        establish_tcp_t.start()
        """ ARD NOT CONNECTED
        """
        #establish_ard_t.start()
        establish_bluetooth_t.start()
        c.start()
        
        #SUBSYSTEM JOINING THREADS
        establish_tcp_t.join()
        """ ARD NOT CONNECTED
        """
        #establish_ard_t.join()
        establish_bluetooth_t.join()
        
                
        #LISTENING STARTING THREADS
        listen_bluetooth_t.start()
        listen_tcp_t.start()
        listen_camera.start()
        #listen_arduino_t.start()
        
        #WRITING STARTING THREADS
        write_bluetooth_t.start()
        write_tcp_t.start()
        #write_ard_t.start()
        
        #LISTENING JOIN THREADS
        listen_bluetooth_t.join()
        listen_tcp_t.join()
        listen_camera.join()
        #listen_arduino_t.join()
        
        #WRITING JOIN THREADS
        write_bluetooth_t.join()
        write_tcp_t.join()
        #write_ard_t.join()

        #CAMERA PROCESS TO RUN TILL THE END
        c.join()

        


if __name__ == "__main__":
    try:
        m = Thread(target=mainthreading)  # start main thread
        m.setDaemon(True)
        m.start()
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("Main Thread killed")
        sys.exit()
