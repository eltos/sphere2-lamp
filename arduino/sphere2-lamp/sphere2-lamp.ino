#include "FastLED.h"  // v3.4.0 http://fastled.io/docs/3.1/
#include <math.h>

/*
 * Sphere² Lamp
 * 
 * A lamp of 122 RGB LED illuminated domes placed on a sphere.
 * 
 */

/* Configuration */
#define DATA_PIN    6
#define POWER       5*5000   // in mW
#define BRIGHTNESS  200
#define LED_TYPE    NEOPIXEL
#define NUM_LEDS    122
CRGBArray<NUM_LEDS> leds;
/* polar angle theta in range 0 (north pole) to 255 (south pole) */
static uint8_t THETA[NUM_LEDS] = {0,26,26,26,26,26,53,45,53,45,53,45,53,45,53,45,69,83,69,90,69,83,69,90,69,83,69,90,69,83,69,90,69,83,69,90,100,112,100,116,100,112,100,116,100,112,100,116,100,112,100,116,100,112,100,116,128,128,128,128,128,128,128,128,128,128,143,155,139,155,143,155,139,155,143,155,139,155,143,155,139,155,143,155,139,155,172,186,165,186,172,186,165,186,172,186,165,186,172,186,165,186,172,186,165,186,202,210,202,210,202,210,202,210,202,210,229,229,229,229,229,255};
/* azimuthal angle phi in range 0 (meridian) to 255 */
static uint8_t PHI[NUM_LEDS] = {128,204,0,51,102,153,153,178,204,230,0,26,51,76,102,128,138,153,168,178,189,204,219,230,240,0,15,26,36,51,66,76,87,102,117,127,141,153,165,178,192,204,216,230,243,255,12,26,39,51,63,76,90,102,114,128,140,166,191,217,242,13,38,64,89,115,128,140,153,166,178,191,204,217,230,242,0,13,26,38,51,64,76,89,102,115,128,143,153,163,178,194,204,214,230,245,255,10,26,41,51,61,76,92,102,112,128,153,178,204,230,0,26,51,76,102,128,178,230,26,76,128};


void setup() {
  delay(3000); // 3 second delay for recovery

  // initialize FastLED
  FastLED.addLeds<LED_TYPE,DATA_PIN>(leds, NUM_LEDS);
  FastLED.setCorrection(TypicalLEDStrip);
  FastLED.setDither(BRIGHTNESS < 255);
  FastLED.setBrightness(BRIGHTNESS);
  FastLED.setMaxPowerInMilliWatts(POWER);
  FastLED.clear();

  Serial.begin(9600);
}


/**
 * Print number of animation frames per second (FPS) to serial
 */
void serial_print_fps(){
  static uint16_t fps = 0;
  static bool init = false; // skip first execution
  fps++;
  EVERY_N_MILLISECONDS( 10000 ) {
    if (init){
      Serial.print("FPS: ");
      Serial.println(fps/10.);
    }
    init = true;
    fps = 0;
  }
}
