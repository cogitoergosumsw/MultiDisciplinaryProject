from bt import btconnection
from c import Camera
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

        # set up camera
        self.camera = Camera()

        self.btread = Queue(maxsize=0)
        self.btwrite = Queue(maxsize=0)
        self.cameraread = Queue(maxsize=0)
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

    def start_all_threads(self):
        ttt = Thread(target=self.read_frombluetooth)
        tt = Thread(target=self.write_tobluetooth)

        c = Thread(target=self.read_fromcamera)

        ttt.setDaemon(True)
        tt.setDaemon(True)
        c.setDaemon(True)

        ttt.start()
        tt.start()
        c.start()

        ttt.join()
        tt.join()
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
