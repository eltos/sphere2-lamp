#!/usr/bin/env python3

# Sphere² Lamp 
# Interactive 3D model to set LED colors via serial port (USB)
#




# pip install serial numpy vpython
import numpy as np
import vpython as vp
from math import *
import time
import sphere2lamp
import serial.tools.list_ports



R, r = 25/2, 4/2
THETA, PHI = np.array([[ 0.00000000e+00,  3.26179070e-01,  3.26179070e-01,  3.26179070e-01,  3.26179070e-01,  3.26179070e-01,  6.52358140e-01,  5.53574359e-01,  6.52358140e-01,  5.53574359e-01,  6.52358140e-01,  5.53574359e-01,  6.52358140e-01,  5.53574359e-01,  6.52358140e-01,  5.53574359e-01,  8.55957125e-01,  1.01722197e+00,  8.55957125e-01,  1.10714872e+00,  8.55957125e-01,  1.01722197e+00,  8.55957125e-01,  1.10714872e+00,  8.55957125e-01,  1.01722197e+00,  8.55957125e-01,  1.10714872e+00,  8.55957125e-01,  1.01722197e+00,  8.55957125e-01,  1.10714872e+00,  8.55957125e-01,  1.01722197e+00,  8.55957125e-01,  1.10714872e+00,  1.22911672e+00,  1.38208580e+00,  1.22911672e+00,  1.43332779e+00,  1.22911672e+00,  1.38208580e+00,  1.22911672e+00,  1.43332779e+00,  1.22911672e+00,  1.38208580e+00,  1.22911672e+00,  1.43332779e+00,  1.22911672e+00,  1.38208580e+00,  1.22911672e+00,  1.43332779e+00,  1.22911672e+00,  1.38208580e+00,  1.22911672e+00,  1.43332779e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.57079633e+00,  1.75950686e+00,  1.91247594e+00,  1.70826487e+00,  1.91247594e+00,  1.75950686e+00,  1.91247594e+00,  1.70826487e+00,  1.91247594e+00,  1.75950686e+00,  1.91247594e+00,  1.70826487e+00,  1.91247594e+00,  1.75950686e+00,  1.91247594e+00,  1.70826487e+00,  1.91247594e+00,  1.75950686e+00,  1.91247594e+00,  1.70826487e+00,  1.91247594e+00,  2.12437069e+00,  2.28563553e+00,  2.03444394e+00,  2.28563553e+00,  2.12437069e+00,  2.28563553e+00,  2.03444394e+00,  2.28563553e+00,  2.12437069e+00,  2.28563553e+00,  2.03444394e+00,  2.28563553e+00,  2.12437069e+00,  2.28563553e+00,  2.03444394e+00,  2.28563553e+00,  2.12437069e+00,  2.28563553e+00,  2.03444394e+00,  2.28563553e+00,  2.48923451e+00,  2.58801829e+00,  2.48923451e+00,  2.58801829e+00,  2.48923451e+00,  2.58801829e+00,  2.48923451e+00,  2.58801829e+00,  2.48923451e+00,  2.58801829e+00,  2.81541358e+00,  2.81541358e+00,  2.81541358e+00,  2.81541358e+00,  2.81541358e+00,  3.14159265e+00],
                       [ 0.00000000e+00,  1.88495559e+00, -3.14159265e+00, -1.88495559e+00, -6.28318531e-01,  6.28318531e-01,  6.28318531e-01,  1.25663706e+00,  1.88495559e+00,  2.51327412e+00, -3.14159265e+00, -2.51327412e+00, -1.88495559e+00, -1.25663706e+00, -6.28318531e-01,  0.00000000e+00,  2.52053900e-01,  6.28318531e-01,  1.00458316e+00,  1.25663706e+00,  1.50869096e+00,  1.88495559e+00,  2.26122022e+00,  2.51327412e+00,  2.76532802e+00, -3.14159265e+00, -2.76532802e+00, -2.51327412e+00, -2.26122022e+00, -1.88495559e+00, -1.50869096e+00, -1.25663706e+00, -1.00458316e+00, -6.28318531e-01, -2.52053900e-01, -2.44929360e-16,  3.29362848e-01,  6.28318531e-01,  9.27274214e-01,  1.25663706e+00,  1.58599991e+00,  1.88495559e+00,  2.18391128e+00,  2.51327412e+00,  2.84263697e+00,  3.14159265e+00, -2.84263697e+00, -2.51327412e+00, -2.18391128e+00, -1.88495559e+00, -1.58599991e+00, -1.25663706e+00, -9.27274214e-01, -6.28318531e-01, -3.29362848e-01,  0.00000000e+00,  3.14159265e-01,  9.42477796e-01,  1.57079633e+00,  2.19911486e+00,  2.82743339e+00, -2.82743339e+00, -2.19911486e+00, -1.57079633e+00, -9.42477796e-01, -3.14159265e-01,  0.00000000e+00,  2.98955683e-01,  6.28318531e-01,  9.57681378e-01,  1.25663706e+00,  1.55559274e+00,  1.88495559e+00,  2.21431844e+00,  2.51327412e+00,  2.81222981e+00, -3.14159265e+00, -2.81222981e+00, -2.51327412e+00, -2.21431844e+00, -1.88495559e+00, -1.55559274e+00, -1.25663706e+00, -9.57681378e-01, -6.28318531e-01, -2.98955683e-01,  0.00000000e+00,  3.76264631e-01,  6.28318531e-01,  8.80372431e-01,  1.25663706e+00,  1.63290169e+00,  1.88495559e+00,  2.13700949e+00,  2.51327412e+00,  2.88953875e+00,  3.14159265e+00, -2.88953875e+00, -2.51327412e+00, -2.13700949e+00, -1.88495559e+00, -1.63290169e+00, -1.25663706e+00, -8.80372431e-01, -6.28318531e-01, -3.76264631e-01,  0.00000000e+00,  6.28318531e-01,  1.25663706e+00,  1.88495559e+00,  2.51327412e+00, -3.14159265e+00, -2.51327412e+00, -1.88495559e+00, -1.25663706e+00, -6.28318531e-01,  0.00000000e+00,  1.25663706e+00,  2.51327412e+00, -2.51327412e+00, -1.25663706e+00,  0.00000000e+00]])


def xyz(theta, phi, R=1.0):
    """Calculate cartesian from spherical coordinates"""
    x = R * sin(theta) * sin(phi)
    y = R * sin(theta) * cos(phi)
    z = R * cos(theta)
    return np.array((x, y, z))



# setup scene

print('Opening model in browser...')

scene = vp.canvas(title='Interactive model of the Sphere² Lamp', background=vp.color.white, width=600, height=600)
scene.caption = '\n'

sphere = vp.sphere(radius=R, pickable=False, shininess = 0)
sphere.rotate(angle=pi/2)
vp.cone(pos=vp.vector(0,0,-2.5*R), radius=R/5, axis=vp.vector(0,0,R), color=vp.color.black)

balls, labels = [], []
for i, (theta, phi) in enumerate(zip(THETA, PHI)):
    balls.append(vp.sphere(pos=vp.vector(*xyz(theta, phi, R)), radius=r, shininess = 0, color=vp.vector(0.5,0.5,0.5)))
    labels.append(vp.label(pos=vp.vector(*xyz(THETA[i], PHI[i], R+r/2)), text=i+1, visible=False))



# view reset

def reset_view(m=None):
    scene.center = vp.vector(0, 0, 0)
    scene.forward = vp.vector(*xyz(3/4*pi, -3/4*pi, R))
    scene.up = vp.vector(0, 0, 1)
    scene.fov = pi/10
    scene.range = 1.5*R
    
scene.append_to_caption('\nHold right mouse button to rotate view ')
vp.button(text='Reset view', bind=reset_view)
reset_view()



# select by mouseclick

selected_led = None

def on_mouse(evt):
    global selected_led
    try:
        obj = scene.mouse.pick
    except:
        obj = None

    # show number of selected led
    for l in labels: l.visible = False
    if obj in balls:
        selected_led = balls.index(obj)
        labels[selected_led].visible = True
    else:
        selected_led = None
    
    on_selected_led_changed()
        
scene.append_to_caption('\n')
scene.bind('click', on_mouse)



# connect/disconnect serial

lamp = None

def update_connect():
    if lamp is None:
        connect_button.text = 'Press enter to connect'
        connect_button.disabled = True
        connect_input.disabled = False
    else:        
        connect_button.text = 'Disconnect lamp'
        connect_button.disabled = False
        connect_input.disabled = True

def connect(evt):
    global lamp
    if lamp is None:
        port = connect_input.text
        try:
            connect_text.text = 'Connecting to ' + port + '...'
            lamp = sphere2lamp.Sphere2lamp(port)
            if not lamp.is_open(): lamp.open()
            time.sleep(3)
            lamp.set_on()
            lamp.set_mode(sphere2lamp.MODE_MANUAL)
            for ball in balls:
                ball.color = vp.vector(0.5,0.5,0.5)
            connect_text.text = 'Connected to ' + lamp.ser.port
        except Exception as e:
            print(e)
            lamp = None
            connect_text.text = 'Failed to connect: ' + str(e)
    else:
        lamp.close()
        lamp = None
        connect_text.text = ''
    update_connect()

scene.append_to_caption('\nPort: ')
ports = serial.tools.list_ports.comports()
default_port = ports and ports[0].name or ''
connect_input = vp.winput(text=default_port, type='string', bind=connect)
connect_button = vp.button(text='Press enter to connect', bind=connect, disabled=True)
connect_text = vp.wtext()


# set color for selected LED

led_color = vp.vector(1, 0, 0)

def select_color(evt):
    global led_color
    for i in range(3):
        color_sliders_text[i].text = ' {:.0f}'.format(color_sliders[i].value)
    hsv = vp.vector(*[color_sliders[i].value/255 for i in range(3)])    
    led_color = vp.color.hsv_to_rgb(hsv)
    color_button.background = led_color
    color_button.color = vp.color.black if led_color.mag > 1.1 else vp.color.white

def pick():
    global led_color
    if selected_led is not None:
        led_color = balls[selected_led].color
        hsv = vp.color.rgb_to_hsv(led_color)
        for i in range(3):
            color_sliders[i].value = 255*hsv.value[i]
            color_sliders_text[i].text = ' {:.0f}'.format(color_sliders[i].value)
        color_button.background = led_color
        color_button.color = vp.color.black if led_color.mag > 1.1 else vp.color.white

def paint(evt=None):
    global led_color
    if selected_led is not None:
        balls[selected_led].color = led_color
        if lamp is not None:
            bytes = [int(round(255*c)) for c in led_color.value]
            try:
                lamp.led_rgb(selected_led, *bytes)
            except Exception as e:
                print(e)
                connect_text.text = 'Error setting LED color to ' + str(bytes) + ': ' + str(e)
            else:
                connect_text.text = 'LED color set'
        
        

def on_selected_led_changed():
    if selected_led is not None:
        if color_paint.checked:
            paint()
        else:
            pick()
            
    

scene.append_to_caption('\n\nColor: ')
color_sliders, color_sliders_text = [], []
for i in range(3):
    scene.append_to_caption('\n  ' + ['Hue','Sat','Val'][i] + ': ')
    color_sliders.append(vp.slider(length=200, left=10, min=0, max=255, value=255, bind=select_color))
    color_sliders_text.append(vp.wtext(text=' 255'))
scene.append_to_caption('\nSelect LED with left mouse button\n')
color_button = vp.button(text='     Paint LED     ', bind=paint, background=led_color)
color_paint = vp.checkbox(text='Paint upon select', bind=lambda e: None, checked=False)



