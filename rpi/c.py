# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import numpy
import time
import cv2

class Camera:
    def __init__(self):
        # initialize the camera and grab a reference to the raw camera capture
        camera = PiCamera()
        camera.resolution = (320,240)
        camera.framerate = 30
        self.camera = camera
        self.rawCapture = PiRGBArray(camera, size=(320, 240))
        self.template = cv2.imread("arrow.jpg",cv2.IMREAD_GRAYSCALE)
        # allow the camera to warmup
        time.sleep(0.2)

    
    def rescaletemplate(self):
        #Reading the template
        return cv2.resize(self.template,(0,0),fx=0.3,fy=0.3)
    
    def rotation(self,img,angle):
        #apply rotation
        (h, w) = img.shape[:2]
        center = (w / 2, h / 2)
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        rotated =  cv2.warpAffine(img, M, (h, w))
        return rotated

    

    def startcamera(self):
        # capture frames from the camera
        rawCapture = self.rawCapture
        camera = self.camera
        for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
        # and occupied/unoccupied text
            image = frame.array
            gray_img = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
            arrow = self.rescaletemplate()
            
            
            angle = 0
            #rotation of arrow and checking here
            while angle < (360):
                check = False
                arrow = self.rotation(arrow,angle)
                match = cv2.matchTemplate(gray_img,arrow,cv2.TM_CCOEFF_NORMED)
                found = numpy.where(match > 0.5)
                w,h = arrow.shape[::-1]
                for point in zip(*found[::-1]):
                    cv2.rectangle(image,point,(point[0]+w,point[1]+h),(255,255,255),1)
                    check = True
                if (check):
                    cv2.imwrite("detected.jpg",image)
                    print ("detected arrow orientatation :%d"%(angle))
                    cv2.imshow("ARROW",image)
                    break;

                angle += 90 
        

        # show the frame
            cv2.imshow("Frame", image)
            key = cv2.waitKey(1) & 0xFF

        # clear the stream in preparation for the next frame
            rawCapture.truncate(0)

        # if the `q` key was pressed, break from the loop
            if key == ord("q"):
                break
       
              
if __name__ == "__main__":
        c = Camera();
        c.startcamera();

  
