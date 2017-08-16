#!/usr/bin/env python

import dothat.lcd as lcd
import sys


text1 = sys.argv[1]
text2 = sys.argv[2]
text3 = sys.argv[3]

lcd.set_cursor_position(0,0)
if(text1 == "null"):
	lcd.write("")
else:
	lcd.write(text1)

lcd.set_cursor_position(0,1)
if(text2 == "null"):
	lcd.write("")
else:
	lcd.write(text2)

lcd.set_cursor_position(0,2)
if(text3 == "null" ):
	lcd.write("")
else:	
	lcd.write(text3)

