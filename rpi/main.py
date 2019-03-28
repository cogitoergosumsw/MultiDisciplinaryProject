from bt import btconnection
from c import Camera
from arduino import ArduinoSerialCon
from rpi_pc_conn import TCPConnection
from multiprocessing import Queue
from multiprocessing import Process
from threading import Thread, current_thread
import time
import sys
import os


class mainthreading(Thread):
    def __init__(self):
        Thread.__init__(self)
        print(current_thread(), 'Main Thread')
        # list of messages to subsystems
        self.btlist = Queue(maxsize=0)
        self.arduinolist = Queue(maxsize=0)
        self.tcplist = Queue(maxsize=0)
        # create queue for camera threads
        self.cameraaa = Queue(maxsize=0)


        self.start_all_threads()

    def info(self, title):
        print(title)
        if hasattr(os, 'getppid'):  # only available on Unix
            print('parent process:', os.getppid())
        print('child process id:', os.getpid())

    def arduinor_p(self,tcplist):
        self.info('Arduino read process')
        while True:
            data = self.arduino.read()
            if (data and 'SENSOR_DATA|' in data):
                tcplist.put_nowait(data)
                print("Arduino Sensor data recieved was : %s AT %s" % (data,time.ctime()))

                
    def arduinow_p(self,arduinolist):
        self.info('Arduino Write process')
        while True:
                if (arduinolist.qsize() > 0):
                    message = arduinolist.get(True,10)
                    self.arduino.send(message)
                    print("Arduino data sent was : %s AT %s" % (message,time.ctime()))
            
                


    # process to start camera, place queue
    def startcamera(self):
        self.info('Camera process')
        self.camera = Camera(self.cameraaa)

    # process to read from bluetooth
    def bluetoothr_p(self,btlist,tcplist,arduinolist):
        self.info('Bluetooth read process')
        status = ''

        while (True):

                    
            data = self.bt.listen_msg()
            try:
                if (len(data) > 0):
                    if ("SET_STATUS" in data):
                            self_message = data.split('|')
                            status = self_message[1][:-1]
                    if ("GET_STATUS" in data):
                            self_message = 'STATUS|'+status+';'
                            btlist.put_nowait(self_message)
                
                    message = data.split(';')
                    for subdata in message:
                        if (len(subdata) > 0):
                                print (" Bluetooth data recieved was : %s AT %s"%(subdata,time.ctime()))
                                tcplist.put_nowait(subdata)
                                if (subdata == 'F' or subdata ==  'L' or subdata == 'R'  or subdata == 'C' or 'MOVE|' in  subdata):
     #                                   print "ENTERED INTO ARDUINO LIST"
      #                                  print subdata
                                        arduinolist.put_nowait(subdata)  # add to aurdino deque

            except Exception as e:
                print("Exception in BT read %s" % e)
                
    #process to write to bluetooth            
    def bluetoothw_p(self,btlist):
        self.info('Bluetooth write process')
        while (True):
            if (btlist.qsize() > 0):
                message = btlist.get(True,10)
                self.bt.send_msg(message)  # put it into the bluetooth queue
                print("Bluetooth data sent was: %s at %s" % (message,time.ctime()))
                


    # process to write data from TCP 
    def TCPw_p(self,tcplist):
        self.info('TCP Write process')
        while True:
            if (tcplist.qsize() > 0):
                message = tcplist.get(True,10)
                self.tcp.tcp_write(message+';\n')
                
                
    # process to read from TCP            
    def TCPr_p(self,arduinolist,btlist):
        self.info('TCP Read process')
        while True:
            data = self.tcp.tcp_read()
            try:
                if data != '':
    #                print("Algorithim data recieved was: %s AT %s" % (data,time.ctime()))
                    messages = data.split(";")
                    for message in messages:
                        if (len(message) > 1):
                                if ("BOT_START" in message):
                                        arduinolist.put_nowait('s')
                                if ("MOVE|" in message):
                                        message = message.split('|')
                                        message = message[1]
                                        btlist.put_nowait("MOVE|" + message+ ";")
                                        arduinolist.put_nowait(message)
                                else:
					btlist.put_nowait(message+';')
                                        if ("BOT|" in message):
                                          if not (self.cameraaa.empty()):
                                            cmessage = self.cameraaa.get()
                                            btlist.put_nowait(cmessage)  
                                           
            
            except KeyboardInterrupt:
                    self.tcp.close()
                    print "TCP killed"
                    break
            except Exception as e:
                self.tcp.close()
                print("Exception data from TCP Connection || Error: %s" % e)
                break
            
        print(current_thread(), "TCP Connection read thread ended")



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
        
        #PROCESSES 
        bluetoothr_p = Process(target=self.bluetoothr_p,args=(self.btlist,self.tcplist,self.arduinolist)) #Read BT process
        bluetoothw_p = Process(target=self.bluetoothw_p,args=(self.btlist,)) #Write BT process
        arduinor_p = Process(target=self.arduinor_p,args = (self.tcplist,)) #Arduino read and write process
        arduinow_p = Process(target=self.arduinow_p,args = (self.arduinolist,)) #Arduino read and write process
        TCPw_p = Process(target=self.TCPw_p,args = (self.tcplist,)) #TCP write process
        TCPr_p = Process(target=self.TCPr_p,args=(self.arduinolist,self.btlist)) #TCP read process
        c = Process(target=self.startcamera) #Start camera process

               
        #PROCESS DAEMONS
        bluetoothr_p.daemon=True
        bluetoothw_p.daemon = True
        TCPw_p.daemon = True
        TCPr_p.daemon = True
        arduinor_p.daemon = True
        arduinow_p.daemon = True
        c.daemon = True

        #SUBSYSTEM DAEMON THREAD
        establish_tcp_t.setDaemon(True)
        establish_ard_t.setDaemon(True)
        establish_bluetooth_t.setDaemon(True)
        
        #SUBSYSTEM STARTING Threads
        establish_tcp_t.start()
        establish_ard_t.start()
        establish_bluetooth_t.start()
        
        #SUBSYSTEM JOINING Threads
        establish_tcp_t.join()
        establish_ard_t.join()
        establish_bluetooth_t.join()
      
        # STARTING PROCESS
        c.start()
        bluetoothr_p.start()
        bluetoothw_p.start()
        TCPw_p.start()
        TCPr_p.start()
        arduinor_p.start()
        arduinow_p.start()
        
        #JOIN PROCESS
        bluetoothr_p.join()
        bluetoothw_p.join()
        TCPw_p.join()
        TCPr_p.join()
        arduinor_p.join()
        arduinow_p.join()
        c.join()

        


if __name__ == "__main__":
    try:
        os.system("sudo hciconfig hci0 piscan")
        m = Thread(target=mainthreading)  # start main thread
        m.setDaemon(True)
        m.start()
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("Main Thread killed")
        sys.exit()
