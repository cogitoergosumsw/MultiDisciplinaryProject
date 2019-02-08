from bt import btconnection
from c import Camera
from arduino import ArduinoSerialCon
from multiprocessing import Queue
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

        # set up arduino connection
        self.arduino = ArduinoSerialCon()
        self.arduino.listen()

        # set up camera
        self.camera = Camera()

        self.btread = Queue(maxsize=0)
        self.btwrite = Queue(maxsize=0)
        self.cameraread = Queue(maxsize=0)
        self.arduino_read = Queue(maxsize=0)
        self.start_all_threads()

    def read_fromcamera(self):
        print(current_thread(), 'camera read Thread')

        camera = self.camera
        while True:
            data = camera.startcamera()
            try:
                if len(data) > 0:
                    print("Data from camera was :%s" % data)
                    self.cameraread.put_nowait(data)
            except:
                print("Error writing from camera")

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

    def write_tobluetooth(self):
        bt = self.bt
        btwrite = self.btwrite
        cameraread = self.cameraread

        print(current_thread(), 'BT write Thread')

        try:
            while True:
                # insert data into bluetooth write queue here
                if not cameraread.empty():  # if there is data from camera
                    btwrite.put_nowait(cameraread.get_nowait())  # put it into the bluetooth queue

                if not btwrite.empty():
                    bt.send_msg(btwrite.get_nowait())  # if queue is not empty
        except:
            print("Exception writing BT")

    def read_fromarduino(self):
        # reading data from Arduino
        print(current_thread(), 'Arduino read Thread')

        while True:
            data = arduino.receive()
            if len(data > 0):
                # TODO pass data to pc/android
                print("%s: Data from Arduino || Data: %s" % (time.ctime(), data))

    def write_toarduino(self):
        # writing data to Arduino
        print(current_thread(), 'Write to Arduino Thread')

        while True:
            time.sleep(0.5)
            # TODO get message from other components
            # arduino.send(message)
            print("%s: Sending data to Arduino: %s" % (time.ctime(), message))

    def start_all_threads(self):

        read_from_bt_thread = Thread(target=self.read_frombluetooth)
        write_to_bt_thread = Thread(target=self.write_tobluetooth)
        camera_thread = Thread(target=self.read_fromcamera)
        read_from_arduino_thread = Thread(target=self.read_fromarduino)
        write_to_arduino_thread = Thread(target=self.write_toarduino)

        read_from_bt_thread.setDaemon(True)
        write_to_bt_thread.setDaemon(True)
        camera_thread.setDaemon(True)
        read_from_arduino_thread.setDaemon(True)
        write_to_arduino_thread.setDaemon(True)

        read_from_bt_thread.start()
        write_to_bt_thread.start()
        camera_thread.start()
        read_from_arduino_thread.start()
        write_to_arduino_thread.start()

        read_from_bt_thread.join()
        write_to_bt_thread.join()
        camera_thread.join()
        read_from_arduino_thread.join()
        write_to_arduino_thread.join()


if __name__ == "__main__":
    try:
        m = Thread(target=mainthreading)  # start main thread
        m.setDaemon(True)
        m.start()
        while True: time.sleep(1)
    except KeyboardInterrupt:
        print("Main Thread killed")
        sys.exit()
s