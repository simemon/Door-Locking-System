#from __future__ import print_function

from datetime import date, datetime, timedelta

import mysql.connector
import sys
import os

HOST = '127.0.0.1'
USERNAME = 'root'
DATABASE = 'ulock'
PASSWORD = 'root'

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

#import subprocess
#subprocess.call(["php","index.php"]);

