#echo mmc0 > /sys/class/leds/led0/trigger		#To reset LED to its default scenario
#echo 1 > /sys/class/leds/led0/brightness		#To set LED (programmable)
#echo 0 > /sys/class/leds/led0/brightness		#To reset LED (programmable)

import RPi.GPIO as GPIO
import time

import cv2

from datetime import date, datetime, timedelta
import mysql.connector
import sys
import os

#database Related Variables

HOST = '127.0.0.1'
USERNAME = 'root'
DATABASE = 'ulock'
PASSWORD = 'root'

IMAGE_NAME = 'test.png'

#Global Part

#GPIO Related Stuff
GPIO.setmode(GPIO.BOARD)
MATRIX = [ [1,2,3,'A'],
	   [4,5,6,'B'],
	   [7,8,9,'C'],
	   ['*',0,'#','D']  ]
ROW = [7,11,13,15]
COL = [12,16,18,22]
EXPECTED_PIN = [1,2,3,4]

#Init
for j in range(4):
	GPIO.setup(COL[j], GPIO.OUT)
	GPIO.output(COL[j], 1)
for i in range(4):
	GPIO.setup(ROW[i], GPIO.IN, pull_up_down = GPIO.PUD_UP)

key = []
def handleGPIO():
	global key
	try:
		while(True):
			for j in range(4):
				GPIO.output(COL[j],0)
				for i  in range(4):
					if GPIO.input(ROW[i]) == 0:
						print MATRIX[i][j]
						if(MATRIX[i][j] == 'D'):
							return				
						key.append(MATRIX[i][j])
						time.sleep(0.3)
						while(GPIO.input(ROW[i]) == 0):
							pass
				GPIO.output(COL[j],1)
	except KeyboardInterrupt:
		GPIO.cleanup()

# Captures a single image from the camera and returns it in PIL format
def get_image(camera):
	 # read is the easiest way to get a full image out of a VideoCapture object.
	 retval, im = camera.read()
	 return im

def capture_image():
	# Camera 0 is the integrated web cam on my netbook
	camera_port = 0
	 
	#Number of frames to throw away while the camera adjusts to light levels
	ramp_frames = 30
	 
	# Now we can initialize the camera capture object with the cv2.VideoCapture class.
	# All it needs is the index to a camera port.
	camera = cv2.VideoCapture(camera_port)

	# Ramp the camera - these frames will be discarded and are only used to allow v4l2
	# to adjust light levels, if necessary
	for i in xrange(ramp_frames):
		temp = get_image(camera)
	print("Taking image...")
	# Take the actual image we want to keep
	camera_capture = get_image(camera)
	file = "test_image.png"
	# A nice feature of the imwrite method is that it will automatically choose the
	# correct format based on the file extension you provide. Convenient!
	cv2.imwrite(file, camera_capture)
	 
	# You'll want to release the camera, otherwise you won't be able to create a new
	# capture object until your script exits
	del(camera)

handleGPIO()
for i in key:
	print i,
print

if(key == EXPECTED_PIN):
	print 'SUCCESS'

capture_image()

IMAGE_NAME = 'test.png'
TIMESTAMP = datetime.now()

db = mysql.connector.connect(host=HOST,user=USERNAME,password=PASSWORD,database=DATABASE)
insert_str = "insert into entry (imageloc,timestamp,valid,seen) values ('%s', '%s', %d, %d)" % (IMAGE_NAME,TIMESTAMP,1,0)

cursor = db.cursor()
cursor.execute(insert_str)

print insert_str
	  	
print("Committed")
db.commit()

cursor.close()
db.close()


