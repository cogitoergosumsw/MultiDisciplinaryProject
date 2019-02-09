# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
from threading import Thread
import numpy
import time
import cv2

class Camera:
    def __init__(self):
        # initialize the camera and grab a reference to the raw camera capture
        camera = PiCamera()
        camera.resolution = (640,480)
        camera.framerate = 60
        self.camera = camera
        self.rawCapture = PiRGBArray(camera, size=(640, 480))
        self.template = cv2.imread("arrow.jpg",cv2.IMREAD_GRAYSCALE)
        # allow the camera to warmup
        time.sleep(0.2)
        


    
    def rescaletemplate(self):
        #Reading the template
        return cv2.resize(self.template,(0,0),fx=0.5,fy=0.5)
    
    def rotation(self,img,angle):
        #apply rotation
        (h, w) = img.shape[:2]
        center = (w / 2, h / 2)
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        rotated =  cv2.warpAffine(img, M, (h, w))
        return rotated
        
    def check(self,qq,image):
        gray_img = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
        arrow = self.rescaletemplate()
            
            
        angle = 0
        #rotation of arrow and checking here
        while angle < (360):
            check = False
            arrow = self.rotation(arrow,angle)
            match = cv2.matchTemplate(gray_img,arrow,cv2.TM_CCOEFF_NORMED)
            found = numpy.where(match > 0.75)
            w,h = arrow.shape[::-1]
            
            if (zip(*found)):
                message =  ("detected arrow orientatation :%d"%(angle))
                print message
                qq.put(message)
                return
            angle += 90
        return
        


    def startcamera(self,qq):
        # capture frames from the camera
        rawCapture = self.rawCapture
        camera = self.camera
        for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
            image = frame.array
            
            T1 = Thread(target=self.check,args=(qq,image,))
            T1.setDaemon(True)
            T1.start()
            #self.check(image)
            key = cv2.waitKey(1) & 0xFF #[?]
            cv2.imshow("im",image)
            rawCapture.truncate(0)
            if key == ord("q"): 
                break
            

       
 
"""if __name__ == "__main__":
        c = Camera();
        c.startcamera();"""
  
