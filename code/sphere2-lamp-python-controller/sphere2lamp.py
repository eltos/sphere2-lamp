#!/usr/bin/env python3

# Serial driver for Sphere² Lamp
#


import serial  # pip install pyserial


# action bytes
ACTION_OFF = 0
ACTION_ON = 1
ACTION_BRIGHTNESS = 2
ACTION_MODE = 10
ACTION_BPM = 11
ACTION_PALETTE = 12
ACTION_TIME_FUNCTION = 13
ACTION_COLOR = 14
ACTION_LED_MAP = 15
ACTION_SET_LED = 101
ACTION_SET_ALL = 102

# mode bytes
MODE_MANUAL = 0
MODE_SOLID = 1
MODE_ANIM_MAP = 2
MODE_ANIM_SOLID = 3
MODE_ANIM_ROTATING_GRADIENT = 15
MODE_ANIM_POLAR_GRADIENT = 16
MODE_ANIM_AZIMUTH_GRADIENT = 17

# palette bytes
PALETTE_DEFAULT_RAINBOW = 0
PALETTE_RED_STRIPE = 1
PALETTE_PARTY = 2

# time functions
TIME_FUNCTION_SAWTOOTH = 0
TIME_FUNCTION_SAWTOOTH_REVERSE = 1
TIME_FUNCTION_TRIANGULAR = 2
TIME_FUNCTION_SINUSOIDAL = 3
TIME_FUNCTION_QUADWAVE = 4

# led maps
LED_MAP_PENTAGON = 0
LED_MAP_HEART = 1
LED_MAP_KRAKEN = 2
LED_MAP_KRAKEN_BG = 3
LED_MAP_SNAKE = 4




class Sphere2lamp:

    def __init__(self, serial_port):
        """Serial controller for Sphere² Lamp
        :param serial_port: serial port
        """
        self.ser = serial.Serial(serial_port, timeout=5)

    def __del__(self):
        self.close()
    
    def open(self):
        """(Re-)open serial connection"""
        self.ser.open()
    
    def close(self):
        """Close serial connection"""
        self.ser.close()
    
    def _send(self, action, *data):
        """Send action and payload
        :param action: action byte
        :param data: payload
        """
        self.ser.flushInput()
        # send single action byte
        assert self.ser.write(serial.to_bytes([action])), 'Failed to send action request'
        # wait for acknowledgement
        assert self.ser.read() == serial.to_bytes([action]), 'Action request not acknowledged'
        # send payload data
        assert self.ser.write(serial.to_bytes(data)) == len(data), 'Failed to send data'
    
    def on(self):
        """Turns the lamp ON"""
        self._send(ACTION_ON)
        
    def off(self):
        """Turns the lamp OFF"""
        self._send(ACTION_OFF)
    
    def brightness(self, value):
        """Sets the overall brightness
        :param value: brightness (0..255)
        """
        self._send(ACTION_BRIGHTNESS, value)
    
    def mode(self, value):
        """Set the mode
        :param value: one of the MODE_* constants
        """
        self._send(ACTION_MODE, value)
    
    def bpm(self, value):
        """Set the animation speed
        :param value: speed in beats per minute (0..255)
        """
        self._send(ACTION_BPM, value)

    def palette(self, value):
        """Set the color palette
        :param value: one of the PALETTE_* constants
        """
        self._send(ACTION_PALETTE, value)
    
    def color_rgb(self, r, g, b):
        """Set the color as RGB
        :param r: red color component (0..255)
        :param g: green color component (0..255)
        :param b: blue color component (0..255)
        """
        self._send(ACTION_COLOR, 0, r, g, b) 
    
    def color_hsv(self, h, s, v):
        """Set the color as HSV
        :param h: color hue (0..255)
        :param s: color saturation (0..255)
        :param v: color value (0..255)
        """
        self._send(ACTION_COLOR, 1, h, s, v) 

    def time_function(self, value):
        """Set the time function
        :param value: one of the TIME_FUNCTION_* constants
        """
        self._send(ACTION_TIME_FUNCTION, value)

    def led_map(self, value):
        """Set the led map
        :param value: one of the LED_MAP_* constants
        """
        self._send(ACTION_LED_MAP, value)

    def led_rgb(self, i, r, g, b):
        """Set the color of an individual LED as RGB
        :param i: LED index, or 255 to set all at once
        :param r: red color component (0..255)
        :param g: green color component (0..255)
        :param b: blue color component (0..255)
        """
        self._send(ACTION_SET_LED, i, 0, r, g, b)

    def led_hsv(self, i, h, s, v):
        """Set the color of an individual LED as HSV
        :param i: LED index, or 255 to set all at once
        :param h: color hue (0..255)
        :param s: color saturation (0..255)
        :param v: color value (0..255)
        """
        self._send(ACTION_SET_LED, i, 1, h, s, v)

    def all_rgb(self, rgbrgbrgb):
        """Set the color of all LEDs as RGB
        :param rgbrgbrgb: LED colors as RGB (r1,g1,b1, r2,g2,b2, ...) each in range (0..255)
        """
        self._send(ACTION_SET_ALL, *rgbrgbrgb)







if __name__ == '__main__':
    
    lamp = Sphere2lamp('COM6')
    lamp.on()
    lamp.brightness(200)
    lamp.close()


