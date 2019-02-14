# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
from threading import Thread
import numpy as np
import math
import time
import cv2

class Camera:
    def __init__(self,que):
        # initialize the camera and grab a reference to the raw camera capture
        camera = PiCamera()
        camera.resolution = (640,480)
        camera.framerate = 30
        self.camera = camera
        self.rawCapture = PiRGBArray(camera, size=(640, 480))
        # allow the camera to warmup
        time.sleep(0.2)
        #start capturing
        self.startcamera(que)
        
    def checkangle(self,image,point1,point2,origin):
        cv2.line(image,(point1),(origin),(255,255,255),thickness=2,lineType=8) 
        cv2.line(image,(point2),(origin),(255,255,255),thickness=2,lineType=8)
        #TODO
        #m1 = (point1[1]-origin[1])/(point1[0]-origin[0])
        #m2 = (point2[1]-origin[1])/(point2[0]-origin[0])
        #angle = (math.degrees(math.atan2(m2,m1)))
        #print angle
        #if (abs(angle) == 90 or abs(angle) == 45):
        return True

    def startcamera(self,cameraqueue):
        font = cv2.FONT_HERSHEY_COMPLEX
        # capture frames from the camera
        rawCapture = self.rawCapture
        camera = self.camera
        for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
            image = frame.array
            
            #self.check(image)
            key = cv2.waitKey(1) & 0xFF #[?]
            gray_img = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
            #https://pysource.com/2018/12/29/real-time-shape-detection-opencv-with-python-3/
            (thresh, im_bw) = cv2.threshold(gray_img, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
            im_bw = cv2.GaussianBlur(im_bw,(3,3),0)

            kernel = np.ones((7, 7), np.uint8)
            im_bw = cv2.erode(im_bw,kernel)
            _,contours,_ = cv2.findContours(im_bw,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
            for cnt in contours:


                approx = cv2.approxPolyDP(cnt,0.008*cv2.arcLength(cnt,True),True)
                x = approx.ravel()[0]
                y = approx.ravel()[1]
                cv2.drawContours(image,[approx],0,(0,255,0),5)
                area = cv2.contourArea(cnt)
                if ((area > 2000) ):
                    if len(approx) == 7:
                        #https://www.learnopencv.com/find-center-of-blob-centroid-using-opencv-cpp-python/
                        M = cv2.moments(cnt)
                        cX = int(M["m10"] / M["m00"])
                        cY = int(M["m01"] / M["m00"])
                        center = (cX,cY)
                        cv2.circle(image, center , 5, (255, 255, 255), -1)
                        #https://www.pyimagesearch.com/2016/04/11/finding-extreme-points-in-contours-with-opencv/
                        extLeft = tuple(cnt[cnt[:, :, 0].argmin()][0]) 
                        extRight = tuple(cnt[cnt[:, :, 0].argmax()][0])
                        extTop = tuple(cnt[cnt[:, :, 1].argmin()][0])
                        extBot = tuple(cnt[cnt[:, :, 1].argmax()][0])
                        cv2.circle(image, extLeft, 8, (0, 0, 255), -1)
                        cv2.circle(image, extRight, 8, (0, 255, 0), -1)
                        cv2.circle(image, extTop, 8, (255, 0, 0), -1)
                        cv2.circle(image, extBot, 8, (255, 255, 0), -1)
                        
                        rl = np.linalg.norm(np.asarray(extLeft)-np.asarray(extRight))
                        ud = np.linalg.norm(np.asarray(extTop)-np.asarray(extBot))
                        message ="Arrow at"
                        direction=""
                        if (rl>ud):
                                if (np.linalg.norm(np.asarray(extLeft)-np.asarray(center)) < np.linalg.norm(np.asarray(extRight)-np.asarray(center))):
                                    if self.checkangle(image,extTop,extBot,extRight):
                                            direction = 'left'
                                else:
                                   if self.checkangle(image,extBot,extTop,extLeft):  
                                        direction ="right"
                        else:
                            if  (np.linalg.norm(np.asarray(extTop)-np.asarray(center)) < np.linalg.norm(np.asarray(extBot)-np.asarray(center))):
                                if self.checkangle(image,extLeft,extRight,extBot):  
                                        direction = "down"
                            else:
                                if self.checkangle(image,extRight,extLeft,extTop):
                                        direction = "up"
                        if (len(direction)>0):
                                message = message+direction+str(area)
                                cameraqueue.put(message)
                                cv2.putText(image, "arrow", (x, y), font, 4, (0, 0, 255))

            cv2.imshow("im",image)
            rawCapture.truncate(0)
            if key == ord("q"): 
                break

        
"""
if __name__ == "__main__":
        c = Camera();
        c.startcamera();
"""  
