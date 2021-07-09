# Sphere² Lamp

A lamp of 122 domes on a sphere, illuminated by individually controlled RGB LEDs.
Using animated color effects, the lamp creates a unique atmosphere.
It can be controlled over USB or bluetooth.


### Demo video

[![Demo video](cover.jpg)](https://player.vimeo.com/video/572701831)  
https://player.vimeo.com/video/572701831


### Instructions

Instructions on how to build the lamp can be found in the [instructions](instructions) folder.


### Code

The LED strip is driven by an Arduino Nano. The complete code is located in [code/sphere2-lamp](code/sphere2-lamp). It uses the [FastLED](https://github.com/FastLED/FastLED) library and features a serial interface to control the Sphere² Lamp.

A simple python program to control the Sphere² Lamp over USB can be found in [code/sphere2-lamp-python-controller](code/sphere2-lamp-python-controller).  

For control via bluetooth low energy (BLE), an Arduino Nano 33 BLE is used. Since the FastLED library is not compatible with the Nano BLE as of now, it connects to the Nano via the serial interface. The code for the BLE controller is located in [code/sphere2-lamp-ble-controller](code/sphere2-lamp-ble-controller).

An android app for control of the lamp via BLE can be found in [code/sphere2-lamp-ble-remote-android-app](code/sphere2-lamp-ble-remote-android-app) and is also available from [Google Play](https://play.google.com/store/apps/details?id=com.github.eltos.sphere2lamp).


### License

Copyright 2021  Philipp Niedermayer (github.com/eltos)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see https://www.gnu.org/licenses.
