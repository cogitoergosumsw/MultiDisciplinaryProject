from rpi.bt import btconnection
from rpi.c import Camera
from rpi.arduino import ArduinoSerialCon
from multiprocessing import Queue
import Queue as qq
from threading import Thread, current_thread
import time
import sys


class mainthreading(Thread):
    def __init__(self):

        Thread.__init__(self)

        print(current_thread(), 'Main Thread')

        # set up bluetooth connection
        self.bt = btconnection()
        self.bt.establish_connection()

        # set up camera
        self.camera = Camera()

        self.btread = Queue(maxsize=0)
        self.btwrite = Queue(maxsize=0)
        self.cameraread = Queue(maxsize=0)

        # set up arduino threads
        self.arduino_read = Queue(maxsize=0)
        self.arduino_write = Queue(maxsize=0)

        # create queue for camera threads
        self.cameraaa = qq.Queue()

        self.arduino = ArduinoSerialCon()
        self.arduino.listen()

        self.start_all_threads()

    def read_fromardunio(self):
        print(current_thread(), 'arduino read Thread')
        while True:
            data = self.arduino.read()
            if len(data > 0):
                # TODO pass data to pc/android
                pass
            print("%s: Data from Arduino || Data: %s" % (time.ctime(), data))

    def write_toardunio(self):
        print(current_thread(), 'arduino write Thread')
        while True:
            if not (self.arduino_write.empty()):
                pass

    # print ("%s: Sending data to Arduino: %s" % (time.ctime(), message))

    # listen to camera queue
    def listen_tocamera(self):
        print(current_thread(), 'camera listen Thread')
        while (True):
            if not (self.cameraaa.empty()):
                try:
                    message = self.cameraaa.get()
                    self.cameraread.put_nowait(message)
                except:
                    print
                    "error reading from camera"

    # thread to start camera, place queue into start camera function
    def startcamera(self):
        self.camera.startcamera(self.cameraaa)
        print(current_thread(), 'camera start Thread')

    # thread to read from bluetooth
    def read_frombluetooth(self):
        print(current_thread(), 'BT read Thread')

        bt = self.bt;
        btread = self.btread

        while (True):
            data = bt.listen_msg()
            try:
                if (len(data) > 0):
                    print("Data from bluetooth was: %s" % data)
                    btread.put_nowait(data)  # read from bluetooth
                    print(btread.qsize())
            except:
                print("Exception reading BT")

    # thread to write to bluetooth
    def write_tobluetooth(self):
        bt = self.bt
        btwrite = self.btwrite
        cameraread = self.cameraread
        print(current_thread(), 'BT write Thread')

        try:
            while (True):
                # insert data into bluetooth write queue here
                if (cameraread.empty() == False):  # if there is data from camera
                    btwrite.put_nowait(cameraread.get_nowait())  # put it into the bluetooth queue

                if (btwrite.empty() == False):
                    bt.send_msg(btwrite.get_nowait())  # if queue is not empty
        except:
            print("Exception writing BT")

    def start_all_threads(self):
        ttt = Thread(target=self.read_frombluetooth)
        tt = Thread(target=self.write_tobluetooth)

        c = Thread(target=self.startcamera)
        c1 = Thread(target=self.listen_tocamera)

        ar = Thread(target=self.read_fromardunio)
        aw = Thread(target=self.write_toardunio)

        ttt.setDaemon(True)
        tt.setDaemon(True)
        c.setDaemon(True)
        c1.setDaemon(True)
        ar.setDaemon(True)
        aw.setDaemon(True)

        ttt.start()
        tt.start()
        c.start()
        c1.start()
        ar.start()
        aw.start()

        ttt.join()
        tt.join()
        c.join()
        c1.join()
        ar.join()
        aw.join()


if __name__ == "__main__":
    try:
        m = Thread(target=mainthreading)  # start main thread
        m.setDaemon(True)
        m.start()
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("Main Thread killed")
        sys.exit()
