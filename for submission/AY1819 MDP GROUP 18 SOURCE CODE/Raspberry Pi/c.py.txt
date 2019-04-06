# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera



from multiprocessing import Queue
from threading import Thread
import numpy as np
import math
import time
import cv2

class Camera:
    def __init__(self,que):
        # initialize the camera and grab a reference to the raw camera capture
        camera = PiCamera()
        camera.resolution = (1280,640)
        camera.framerate = 50
        camera.rotation = 180
        camera.video_stabilization = True
        self.camera = camera
        self.rawCapture = PiRGBArray(camera, size=(1280, 640))
        # allow the camera to warmup
        time.sleep(0.2)
        #start capturing
        self.startcamera(que)


        
    def checkangle(self,image,point1,point2,origin):
        line1 = np.linalg.norm(np.asarray(point1)-np.asarray(origin))
        line2 = np.linalg.norm(np.asarray(point2)-np.asarray(origin))
        hypo = np.linalg.norm(np.asarray(point2)-np.asarray(point1))
        
        cv2.line(image,(point1),(origin),(255,255,255),thickness=2,lineType=8) 
        cv2.line(image,(point2),(origin),(255,255,255),thickness=2,lineType=8)
        cv2.line(image,(point1),(point2),(255,0,0),thickness=2,lineType=8) 
        
        angle = round(math.degrees(math.acos((line1**2 + line2**2 - hypo**2)/(2*line1*line2))),1)
        
        if (87<=angle<=92):
                return True
        else:
                return False
                

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
                if ((area > 100) ):
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
                        message=""
                        if (rl>ud):
                                if self.checkangle(image,extTop,extBot,extRight):
                                        message = 'AL|'
                                if self.checkangle(image,extBot,extTop,extLeft):  
                                        message ="AR|"
                        else:
                                if self.checkangle(image,extLeft,extRight,extBot):  
                                        message = "AD|"
                                if self.checkangle(image,extRight,extLeft,extTop):
                                        message = "AU|"
                        if (len(message)>0):
                                area = (area/1000)
                                Grid=''
                                lateral=''
                                if (20<= area):
                                        Grid = 3
                                        if (cX<580):
                                                lateral = 1
                                        if (700<cX<900):
                                                lateral = 2
                                        if (cX>1050):
                                                lateral=3
                                if (9<= area <= 11):
                                        Grid = 4
                                        if (cX<250):
                                                lateral = 0
                                        if (370<cX<580):
                                                lateral = 1
                                        if (680<cX<820):
                                                lateral = 2
                                        if (cX>950):
                                                lateral = 3
                                if (5.9<= area <=7.5):
                                        Grid = 5
                                        if (cX<300):
                                                lateral = 0
                                        if (400<cX<580):
                                                lateral = 1
                                        if (650<cX<820):
                                                lateral = 2
                                        if (cX>1000):
                                                lateral = 3
                                
                                if (message is 'AU|' and lateral is not'' and Grid is not''):
                                        message = message+str(lateral)+","+str(Grid)+';'
                                        print message
                                        cameraqueue.put(message)
                                        cv2.putText(image, "arrow", (x, y), font, 2, (0, 0, 255))
            

            #cv2.imshow("im",image)
            rawCapture.truncate(0)
            if key == ord("q"): 
                break


if __name__ == "__main__":
        placeholder = Queue(maxsize=0)
        c = Camera(placeholder);

 
