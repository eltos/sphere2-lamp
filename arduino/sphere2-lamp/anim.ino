/**
 * Animations for the Sphere² Lamp
 * 
 */


// Default palette
CRGBPalette16 defaultPalette = RainbowColors_p;



/**
 * Function macros producing continuous linear/triangular/sinusoidal/quadratric sweeps with given BPM
 */
#define SAWTOOTH [](accum88 bpm){return beat8(bpm);}               // sawtooth
#define SAWTOOTH_REVERSE [](accum88 bpm){return 255-beat8(bpm);}   // reverse sawtooth
#define TRIANGULAR [](accum88 bpm){return triwave8(beat8(bpm));}   // triangular
#define SINUSOIDAL [](accum88 bpm){return beatsin8(bpm);}          // sinusoidal
#define QUADWAVE [](accum88 bpm){return quadwave8(beat8(bpm));}    // quadratic sinusoidal (spends just a little more time at the limits)

/**
 * Gradient function producing flashes for a linear input
 * 
 * @param x: input parameter linear in range 0 to 255
 * @param flashes: number of flashes
 * @return: function output in range 0 to 255
 */
uint8_t flash(uint8_t x, uint8_t flashes){
  float s = pow(255-x, 2)/255;
  return s*pow(cos(PI*flashes*s/255), 2);
}

/**
 * A version of the flash function with slightly longer initial light up
 * 
 * @param x: input parameter linear in range 0 to 255
 * @param flashes: number of flashes
 * @return: function output in range 0 to 255
 */
uint8_t blink(uint8_t x, uint8_t flashes){
  float s = pow(255-x, 2)/255;
  return s*pow(cos(PI*(flashes+0.25)*s/255), 2);
}




/********************************
 * Texture map based animations *
 ********************************/


/**
 * Predefined maps mapping the LED number (index) to a certain color/hue.
 */
static size_t MAP_PENTAGON[NUM_LEDS] = {0,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,1,2,1,0,1,2,1,0,1,2,1,0,1,2,1,0,1,2,1,0,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,2,2,2,2,2,2,2,2,2,2,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,0,1,2,1,0,1,2,1,0,1,2,1,0,1,2,1,0,1,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,0};
static size_t MAP_HEART[NUM_LEDS] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
static size_t MAP_KRAKEN[NUM_LEDS] = {0,1,1,1,1,1,2,15,2,15,2,15,2,15,2,15,16,3,16,14,16,3,16,14,16,3,16,14,16,3,16,14,16,3,16,14,16,4,16,13,16,4,16,13,16,4,16,13,16,4,16,13,16,4,16,13,16,16,16,16,16,16,16,16,16,16,12,16,5,16,12,16,5,16,12,16,5,16,12,16,5,16,12,16,5,16,11,16,6,16,11,16,6,16,11,16,6,16,11,16,6,16,11,16,6,16,10,7,10,7,10,7,10,7,10,7,9,9,9,9,9,8};
static size_t MAP_SNAKE[NUM_LEDS] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,13,0,4,0,3,0,44,0,43,0,34,0,33,0,24,0,23,0,14,0,12,0,5,0,2,0,45,0,42,0,35,0,32,0,25,0,22,0,15,0,11,6,1,46,41,36,31,26,21,16,0,10,0,7,0,50,0,47,0,40,0,37,0,30,0,27,0,20,0,17,0,9,0,8,0,49,0,48,0,39,0,38,0,29,0,28,0,19,0,18,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
static size_t MAP_EARTH[NUM_LEDS] = {2,2,2,2,2,2,1,2,2,2,3,1,4,4,1,1,1,1,3,3,3,3,3,4,3,4,1,1,1,1,4,4,3,1,1,1,1,1,1,3,3,1,4,4,4,4,4,1,1,3,3,1,1,1,1,1,1,3,1,1,3,1,3,1,1,1,1,1,3,4,1,1,1,1,3,3,1,1,3,3,3,1,1,1,1,1,1,1,4,4,4,1,1,1,3,1,1,1,1,3,3,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2};

/**
 * Predefined texture colors defining the colors
 */
static CRGB MAP_EARTH_COLORS[5] = {CRGB(0,0,0), CRGB(0,0,200), CRGB(255,255,255), CRGB(0,200,0), CRGB(100,150,0)};
static CRGB MAP_RGB_COLORS[3] = {CRGB::Red, CRGB::Green, CRGB::Blue};

/**
 * Build a map with equally distributed hues (linear gradient) as required for the given LED map
 * Make sure to free the hue_map after usage using: free(hue_map);
 * 
 * @param led_map: array mapping LED indices to color indices
 * @param skip_zero: if true, the value 0 in led_map is assigned hue 0
 * @param hue_start: first hue of linear gradient (inclusive)
 * @param hue_stop: last hue of linear gradient (exclusive)
 */
uint8_t* build_hue_map(size_t* led_map, bool skip_zero = false, uint8_t hue_start = 0, uint8_t hue_stop = 255){
  // count required hues
  uint8_t num_hues = 0;
  for (size_t i = 0; i < NUM_LEDS; ++i){
    if (led_map[i] > num_hues) num_hues = led_map[i];
  }
  num_hues++;
  // create equally distributed hue map
  uint8_t *hue_map = (uint8_t *) malloc(num_hues*sizeof(uint8_t));
  if (hue_map == NULL){ return NULL; } // out of RAM
  if (skip_zero){
    hue_map[0] = 0;
    num_hues--;
  }
  for (size_t i = 0; i < num_hues; ++i){
    hue_map[skip_zero ? i+1 : i] = hue_start + (hue_stop - hue_start)*i/num_hues;
  }
  return hue_map;
}


/**
 * Set LED colors based on a texture map
 * 
 * @param led_map: array mapping LED indices to color indices
 * @param map_colors: array with colors
 */
void fill_map(size_t* led_map = MAP_PENTAGON, CRGB* map_colors = MAP_RGB_COLORS){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    leds[i] = map_colors[led_map[i]];
  }
}

/**
 * Set LED colors for a part of a texture map
 * 
 * @param led_map: array mapping LED indices to color indices
 * @param number: number in map to consider
 * @param color: color to set
 */
void fill_submap(size_t* led_map, size_t number, CRGB color){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    if (led_map[i] != number){ continue; }
    leds[i] = color;
  }
}

/**
 * Animate LED color based on a texture map and (initial) hue
 * 
 * @param led_map: array mapping LED indices to color hue index
 * @param map_hues: array with hues to start with for each index
 * @param bpm: hue shift animation speed in cycles (beats) per minute
 * @param palette: color palette to map distances to color
 * @param function8: function to calculate color palette hue shift as function of bpm and time
 * @param brightness: LED brightness
 * @param blending: blending type for color palette
 * @param zero_is_off: if true, the value 0 in led_map has the special meaning of zero brightness
 */
template <typename F = uint8_t(accum88)>
void anim_map(size_t* led_map, uint8_t* map_hues, uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette,
              F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND, bool zero_is_off = false){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    uint8_t hue = function8(bpm) - map_hues[led_map[i]];
    leds[i] = ColorFromPalette(*palette, hue, zero_is_off && led_map[i] == 0 ? 0 : brightness, blending);
  }
}

/**
 * Shortcut for anim_map(led_map, map_hues, ...) dynamically using equally distributed hues
 */
template <typename F = uint8_t(accum88)>
void anim_map(size_t* led_map = MAP_PENTAGON, uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette,
              F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND, bool zero_is_off = false){
  uint8_t *hue_map = build_hue_map(led_map, zero_is_off);
  anim_map(led_map, hue_map, bpm, palette, function8, brightness, blending, zero_is_off);
  free(hue_map);
}

/**
 * Animate LED color based on a part of a texture map
 * 
 * @param led_map: array mapping LED indices to color hue index
 * @param number: number in map to consider
 * @param bpm: hue shift animation speed in cycles (beats) per minute
 * @param palette: color palette to map distances to color
 * @param function8: function to calculate color palette hue shift as function of bpm and time
 * @param brightness: LED brightness
 * @param blending: blending type for color palette
 */
template <typename F = uint8_t(accum88)>
void anim_submap(size_t* led_map, size_t number, uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette,
              F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND, bool zero_is_off = false){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    if (led_map[i] != number) continue;
    uint8_t hue = function8(bpm);
    leds[i] = ColorFromPalette(*palette, hue, brightness, blending);
  }
}




/***************************************
 * Position and angle based animations *
 ***************************************/


/**
 * Animate LEDs based on their distance to a given spherical coordinate
 * 
 * @param theta: polar angle of reference point in range 0 (north pole) to 255 (south pole)
 * @param phi: azimuthal angle of reference point in range 0 to 255
 * @param bpm: hue shift animation speed in cycles (beats) per minute
 * @param palette: color palette to map distances to color
 * @param function8: function to calculate color palette hue shift as function of bpm and time
 * @param brightness: LED brightness
 * @param blending: blending type for color palette
 */
template <typename F = uint8_t(accum88)>
void anim_distance(uint8_t theta, uint8_t phi, uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette,
                   F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    uint8_t hue = distance(THETA[i], PHI[i], theta, phi) + function8(bpm);
    leds[i] = ColorFromPalette(*palette, hue, brightness, blending);
  }
}

template <typename F = uint8_t(accum88)>
void anim_distance_led(size_t led, uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette,
                       F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND){
  anim_distance(THETA[led], PHI[led], bpm, palette, function8, brightness, blending);
}

/**
 * Animate LEDs based on the azimuthal angle
 * 
 * @param bpm: hue shift animation speed in cycles (beats) per minute
 * @param palette: color palette to map angle to color
 * @param tip_color: color of tips with undefined azimuthal angle
 * @param function8: function to calculate color palette hue shift as function of bpm and time
 * @param brightness: LED brightness
 * @param blending: blending type for color palette
 */
template <typename F = uint8_t(accum88)>
void anim_azimuth(uint8_t bpm = 10, CRGBPalette16* palette = &defaultPalette, CRGB tip_color = CRGB::White,
                  F function8 = SAWTOOTH, uint8_t brightness = 255, TBlendType blending = LINEARBLEND){
  leds[0] = tip_color;
  leds[NUM_LEDS-1] = tip_color;
  for (size_t i = 1; i < NUM_LEDS-1; ++i){
    leds[i] = ColorFromPalette(*palette, PHI[i] + function8(bpm), brightness, blending);
  }
}



/*******************************
 * Various animation functions *
 *******************************/


/*
 * Animation counting the LEDS in their internal wireing order 
 * for testing and debugging purposes
 * 
 * @param brightness: maximum LED brightness
 */
void anim_count(uint8_t brightness = 255){
  uint8_t c = beat8(1);
  if (c == 0){
    FastLED.clear();
  } else if (c <= NUM_LEDS){
    uint8_t hue = 255 * ((c-1)%12) / 12;
    leds[c-1] = ColorFromPalette(defaultPalette, hue, brightness, LINEARBLEND);
  }
}

/**
 * Twinkling animation of the full sphere creating a shimmering effect
 * 
 * Every LED cycles through the given color palette using a sinusoidal function 
 * with a random initial phase and slightly different BPMs. The brightniss as a 
 * whole is modulated with twice the given BPM by -20% if enabled.
 * 
 * @param bpm: animation speed in cycles (beats) per minute (actual value is up to 5 larger for some LEDs)
 * @param palette: color palette to use
 * @param brightness: maximum LED brightness 
 * @param modulate_brightness: if true, modulate brightness, i.e. dim by 20%
 */
 void anim_twinkle(uint8_t bpm = 3, CRGBPalette16* palette = &defaultPalette, 
                   uint8_t brightness = 255, bool modulate_brightness = true){
  for (size_t i = 0; i < NUM_LEDS; ++i){
    uint8_t hue = beatsin8(bpm+i%5, 0, 255, 0, inoise8(THETA[i]<<5, PHI[i]<<5)); // noisy phase in 24..240
    uint8_t bright = !modulate_brightness ? brightness : beatsin8(2*bpm+1, scale8(brightness, 200), brightness); //, 0, inoise8(PHI[i]<<5, THETA[i]<<5));
    leds[i] = ColorFromPalette(*palette, hue, bright, LINEARBLEND);
  }
}



 
/**
 * Various overlay animations 
 */


/**
 * Create a rotating searchlight/radar, lighting up and fading the LEDs according to their azimuthal angle phi
 * 
 * @param hue: color hue of searchlight
 * @param bpm: turns per minute
 * @param saturation: color saturation of searchlight
 * @param brightness: color brightness of searchlight
 * @param tail: length of fading-out tail from 0..255
 * @param head: length of fading-in head from 0..255
 * @param ping_led: LED to flash at maximum brightness when hit by the searchlight/radar
 * @param ping_hue: color hue of flash
 * @param blend_over: if true, blends the animation over the existing colors instead of overwriting them
 */
void anim_searchlight(uint8_t hue = 0, uint8_t bpm = 20, uint8_t saturation = 255, uint8_t brightness = 255,
                      uint8_t tail = 100, uint8_t head = 20, size_t ping_led = -1, uint8_t ping_hue = 0,
                      bool blend_over = false){
  if (!blend_over) FastLED.clear();
  for (size_t i = 0; i < NUM_LEDS; ++i){
    uint8_t value = SAWTOOTH(bpm) - PHI[i];
    CRGB color;
    if (i == ping_led){
      value = 255-value;
      color = CHSV(ping_hue, 255, value);
    } else {
      if (i == 0 || i == NUM_LEDS-1){
        value = 255;
      } else if (value < head){
        value = 255.0*value/head;
      } else if (value < head + tail){
        value = 255*(1-(value-head)*1.0/tail);
      } else {
        value = 0;
      }
      color = CHSV(hue, saturation, brightness);
    }
    leds[i] = nblend(leds[i], color, value);
  }
}

/**
 * Make some LEDs flash randomly.
 * The default parameters create a "thunderstorm with lightning on the globe" like animation.
 * 
 * @tparam FLASHLIGHTS: count of flashing LEDs
 * @param color: color of flash animation
 * @param flashing: number of light-dark cycles per flash animation
 * @param duration: duration of flash animation in ms (minimum 25)
 * @param delay: mean delay between animations in ms (everything < 25ms is equal to zero)
 */
template <size_t FLASHLIGHTS = 5>
void anim_add_flashes(CRGB color = CRGB::White, uint8_t flashing = 2, uint16_t duration = 400, uint16_t delay = 500){
  static size_t flash_led[FLASHLIGHTS] = {0};       // LED numbers
  static uint8_t flash_bright[FLASHLIGHTS] = {0};   // current blending brightness
  static uint8_t flash_counter[FLASHLIGHTS] = {0};  // animation frame counter from 255..0
  // calculate flash animation at 40 fps
  EVERY_N_MILLISECONDS(25) {
    for (size_t i = 0; i < FLASHLIGHTS; ++i){
      if (flash_counter[i] < 6375/duration){
        // animation ended
        flash_counter[i] = 0;
        flash_bright[i] = 0;
        // create new spot with probatibility p
        if (!delay || random8() < 6375/delay){ // random8 < 255*p where p = 25/FLASHDELAY
          flash_led[i] = random8(NUM_LEDS);
          flash_counter[i] = 255;
          if (delay && delay <= 25 && random8() < 128){
            flash_counter[i] -= 6375/duration; // make animations diverge even if they start immediately
          }
        }
      } else {
        // animate flash
        flash_counter[i] -= 6375/duration; // counter from 255 to 0      
        flash_bright[i] = flash(flash_counter[i], flashing);
      }
    }
  }
  // overlay flashs (current animation frame) with leds
  for (size_t i = 0; i < FLASHLIGHTS; ++i){
    nblend(leds[flash_led[i]], color, flash_bright[i]);
    //anim_distance(THETA[i], PHI[i], 235/50*DIMMING, &red_stripe, QUADWAVE); // BPM = 235/EVERY_MS*DIMMING
  }
}
