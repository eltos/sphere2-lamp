
/*************
 * MAIN LOOP *
 *************/

/* Address to save lamp state to */
#define EEPROM_ADDR 0


/* State constants */
#define MODE_MANUAL 0
#define MODE_SOLID 1
#define MODE_ANIM_MAP 2
#define MODE_ANIM_SOLID 3
#define MODE_ANIM_ROTATING_GRADIENT 15
#define MODE_ANIM_POLAR_GRADIENT 16
#define MODE_ANIM_AZIMUTH_GRADIENT 17
#define MODE_ANIM_ROTATING_SEARCHLIGHT 18
#define MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE 19
#define MODE_ANIM_TWINKLE 21
#define MODE_ANIM_GLITTER 22
#define MODE_ANIM_SPRINKLE 23
#define MODE_ANIM_SPOTLIGHTS 24
#define MODE_ANIM_SPOTLIGHTS_PALETTE 25
#define MODE_ANIM_POLICE 26

#define PALETTE_DEFAULT_RAINBOW 0
#define PALETTE_RED_STRIPE 1
#define PALETTE_PARTY 2
#define PALETTE_OCEAN 3
#define PALETTE_FOREST 4
#define PALETTE_LAVA 5
#define PALETTE_RGB 6

#define TIME_FUNCTION_SAWTOOTH 0
#define TIME_FUNCTION_SAWTOOTH_REVERSE 1
#define TIME_FUNCTION_TRIANGULAR 2
#define TIME_FUNCTION_SINUSOIDAL 3
#define TIME_FUNCTION_QUADWAVE 4

#define LED_MAP_PENTAGON 0
#define LED_MAP_HEART 1
#define LED_MAP_KRAKEN 2
#define LED_MAP_KRAKEN_BG 3
#define LED_MAP_SNAKE 4
#define LED_MAP_SPIRAL 5


/**
 * Persistent lamp state
 */
struct LampState {
  bool on = true;
  uint8_t brightness = 200;
  uint8_t mode = MODE_ANIM_MAP;
  uint8_t bpm = 30;
  uint8_t time_function = TIME_FUNCTION_SAWTOOTH;
  uint8_t palette = PALETTE_DEFAULT_RAINBOW;
  uint8_t led_map = LED_MAP_PENTAGON;
  CRGB color = CRGB::White; // uint8_t color[3] = {255,255,255};
} state;

// Runtime variables derived from state constants
CRGBPalette16 palette_p = RainbowColors_p;
uint8_t(*time_function_p)(accum88) = SAWTOOTH;
uint8_t led_map_p[NUM_LEDS];
bool led_map_bg_color = false;

/**
 * Method to update run time settings from state
 * and persistently store the state to EEPROM
 */
void stateChanged(){
  FastLED.clear();
  // brightness
  FastLED.setDither(state.brightness < 255);
  FastLED.setBrightness(state.brightness);
  // time function
  switch (state.time_function){
    default:
    case TIME_FUNCTION_SAWTOOTH:            time_function_p = SAWTOOTH;           break;
    case TIME_FUNCTION_SAWTOOTH_REVERSE:    time_function_p = SAWTOOTH_REVERSE;   break;
    case TIME_FUNCTION_TRIANGULAR:          time_function_p = TRIANGULAR;         break;
    case TIME_FUNCTION_SINUSOIDAL:          time_function_p = SINUSOIDAL;         break;
    case TIME_FUNCTION_QUADWAVE:            time_function_p = QUADWAVE;           break;
  }
  // color palette
  switch (state.palette){
    default:
    case PALETTE_DEFAULT_RAINBOW:           palette_p = RainbowColors_p;  break;
    case PALETTE_RED_STRIPE:                palette_p = red_stripe_p;     break;
    case PALETTE_PARTY:                     palette_p = PartyColors_p;    break;
    case PALETTE_OCEAN:                     palette_p = OceanColors_p;    break;
    case PALETTE_FOREST:                    palette_p = ForestColors_p;   break;
    case PALETTE_LAVA:                      palette_p = LavaColors_p;     break;
    case PALETTE_RGB:                       palette_p = rgb_p;            break;
  }
  // led map
  switch (state.led_map){
    default:
    case LED_MAP_PENTAGON:      led_map_bg_color = false; memcpy_P(led_map_p, MAP_PENTAGON,   NUM_LEDS); break;
    case LED_MAP_HEART:         led_map_bg_color = true;  memcpy_P(led_map_p, MAP_HEART,      NUM_LEDS); break;
    case LED_MAP_KRAKEN:        led_map_bg_color = false; memcpy_P(led_map_p, MAP_KRAKEN,     NUM_LEDS); break;
    case LED_MAP_KRAKEN_BG:     led_map_bg_color = true;  memcpy_P(led_map_p, MAP_KRAKEN,     NUM_LEDS); break;
    case LED_MAP_SNAKE:         led_map_bg_color = true;  memcpy_P(led_map_p, MAP_SNAKE,      NUM_LEDS); break;
    case LED_MAP_SPIRAL:        led_map_bg_color = true;  memcpy_P(led_map_p, MAP_SPIRAL,     NUM_LEDS); break;
  }
        
  // save state to EEPROM
  EEPROM.put(EEPROM_ADDR, state);
}

/**
 * Load saved state from EEPROM
 */
void recoverState(){
  EEPROM.get(EEPROM_ADDR, state);
  stateChanged();
}







/* 
 *  Main loop
 *  
 *  Runs animation functions etc. depending on the current state
 */
void loop(){


  if (state.on){ // Lamp ON
    digitalWrite(LED_BUILTIN, HIGH);

    switch (state.mode) {
      // LED colors controlled directly over serial
      case MODE_MANUAL:
        break;

      // solid color or animation
      case MODE_SOLID:
        fill_solid(leds, NUM_LEDS, state.color);
        break;
      case MODE_ANIM_SOLID:
        anim_solid(state.bpm, &palette_p, time_function_p);
        break;

      // animations based on LED texture map
      case MODE_ANIM_MAP:
        anim_map(led_map_p, state.bpm, &palette_p, time_function_p, 255, LINEARBLEND, led_map_bg_color);
        if (led_map_bg_color) fill_submap(led_map_p, 0, state.color);
        break;

      // rotating rainbow gradients
      case MODE_ANIM_ROTATING_GRADIENT: 
        #define THETA_BPM state.bpm/9
        #define PHI_BPM state.bpm/7
        anim_distance(TRIANGULAR(THETA_BPM), SAWTOOTH(PHI_BPM) + 128*(beat8(THETA_BPM) < 128), state.bpm, &palette_p, time_function_p);
        break;
      case MODE_ANIM_POLAR_GRADIENT:
        anim_distance(0, 0, state.bpm, &palette_p, time_function_p);
        break;
      case MODE_ANIM_AZIMUTH_GRADIENT:
        anim_azimuth(state.bpm, &palette_p, state.color, time_function_p);
        break;
      case MODE_ANIM_ROTATING_SEARCHLIGHT:
        anim_searchlight(state.color, state.bpm, 100, 20);
        break;
      case MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE:
        anim_searchlight(&palette_p, state.bpm, time_function_p, state.bpm, 100, 20);
        break;


      // other animations
      case MODE_ANIM_TWINKLE:
        anim_twinkle(state.bpm, &palette_p);
        break;

      case MODE_ANIM_GLITTER:
        FastLED.clear();
        anim_add_flashes<15>(state.color, 2, 500, 60000/state.bpm);
        break;

      case MODE_ANIM_SPRINKLE:
        anim_sprinkle(state.bpm, &palette_p);
        break;

      case MODE_ANIM_SPOTLIGHTS:
        fill_solid(leds, NUM_LEDS, state.color);
        nscale8_video(leds, NUM_LEDS, 15);
        anim_add_lava_blobs(state.color, state.bpm);
        break;

      case MODE_ANIM_SPOTLIGHTS_PALETTE: {
        CRGB color = ColorFromPalette(palette_p, time_function_p(scale8_video(state.bpm, 25)), 255, LINEARBLEND);
        fill_solid(leds, NUM_LEDS, color);
        nscale8_video(leds, NUM_LEDS, 15);
        anim_add_lava_blobs(color, state.bpm);
        break;
      }

      case MODE_ANIM_POLICE:
        anim_police_lights(state.bpm);
        break;
      
    }

  } else { // Lamp OFF
    FastLED.clear();
    digitalWrite(LED_BUILTIN, LOW);
    
  }
  

  
  /* All LEDs on full brightness */
//  fill_solid(leds, NUM_LEDS, CRGB::White);

  /* Lighting up in the internal numbering order for testing */
//  anim_count();

  /* Animations based on LED texture maps */
//  anim_map(MAP_PENTAGON);
//  anim_submap(MAP_PENTAGON, 2, 5);
//  anim_map(MAP_KRAKEN, 10, &red_stripe);
//  anim_map(MAP_SNAKE, 20, &red_stripe, SAWTOOTH, 255, LINEARBLEND, true);
//  anim_map(MAP_SNAKE, 30, &stripes_palette, SAWTOOTH, 255, NOBLEND, true);
//  fill_map(MAP_PENTAGON, MAP_RGB_COLORS);
//  fill_map(MAP_EARTH, MAP_EARTH_COLORS);
//  fill_submap(MAP_HEART, 1, CRGB::Red);

  /* Rotating rainbow gradients */
//  anim_distance(TRIANGULAR(THETA_BPM), SAWTOOTH(PHI_BPM) + 128*(beat8(THETA_BPM) < 128), 17, &defaultPalette);
//  anim_distance(0, 0, 10);
//  anim_distance(TRIANGULAR(THETA_BPM), SAWTOOTH(PHI_BPM) + 128*(beat8(THETA_BPM) < 128), 47, &red_stripe, QUADWAVE);
//  anim_azimuth();
//  anim_azimuth(60, &party_palette, CRGB::Black, QUADWAVE);
//  anim_add_searchlight(CRGB::Blue, 60, /*tail*/ 100, /*head*/ 20);

  /* Revolving alarm light / radar / searchlight */
//  anim_searchlight(HUE_RED, 60);
//  anim_searchlight(20, 60); // orange
//  anim_police_lights(60);

  /* Random sprinkles and twinkles */
//  anim_twinkle(3, &party_palette);
//  anim_sprinkle(30, &party_palette);
//  FastLED.clear(); anim_add_flashes();
//  FastLED.clear(); anim_add_flashes<20>(CRGB::Red, 2, 500, 1);

  /* Globe */
//  fill_map(MAP_EARTH, MAP_EARTH_COLORS); 
//  fill_map(MAP_EARTH, MAP_EARTH_COLORS); anim_searchlight(0, 20, 0, 0, 20, 230, 10, 0, true);
  



  
//  anim_fps();
//  serial_print_fps();
  
  FastLED.show();
  
}
