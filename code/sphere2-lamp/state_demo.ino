/**
 * Demo states, handcrafted and carefully selected
 * 
 */

/**
 * sets the next demo state by cycling trough them
 */
void next_demo_state(){
  static uint8_t demo_state = 0;
  demo_state++;  
  switch (demo_state){
    default:
    case 0:
      demo_state = 0;
      state.mode = MODE_ANIM_SOLID;
      state.bpm = 5;
      state.palette = PALETTE_DEFAULT_RAINBOW;
      state.time_function = TIME_FUNCTION_SAWTOOTH;
      break;
    case 1:
      state.mode = MODE_ANIM_MAP;
      state.color = CRGB::Black;
      state.led_map = LED_MAP_PENTAGON;
      state.bpm = 15;
      break;
    case 2:
      state.led_map = LED_MAP_SPIRAL;
      state.bpm = 30;
      break;
    case 3:
      state.palette = PALETTE_RGB;
      state.bpm = 120;
      break;
    case 4:
      state.mode = MODE_ANIM_POLAR_GRADIENT;
      state.bpm = 30;
      state.palette = PALETTE_PARTY;
      state.time_function = TIME_FUNCTION_SINUSOIDAL;
      break;
    case 5:
      state.mode = MODE_ANIM_AZIMUTH_GRADIENT;
      state.palette = PALETTE_DEFAULT_RAINBOW;
      state.time_function = TIME_FUNCTION_SAWTOOTH;
      state.color = CRGB::White;
      break;
    case 6:
      state.mode = MODE_ANIM_ROTATING_GRADIENT;
      break;
    case 7:
      state.mode = MODE_ANIM_ROTATING_SEARCHLIGHT_PALETTE;
      state.palette = PALETTE_PARTY;
      state.bpm = 100;
      break;
    case 8:
      state.mode = MODE_ANIM_TWINKLE;
      state.bpm = 10;
      state.palette = PALETTE_DEFAULT_RAINBOW;
      break;
    case 9:
      state.mode = MODE_ANIM_SPRINKLE;
      state.palette = PALETTE_PARTY;
      state.bpm = 30;
      break;
    case 10:
      state.mode = MODE_ANIM_GLITTER;
      state.bpm = 30;
      break;
    case 11:
      state.mode = MODE_ANIM_SPOTLIGHTS;
      state.color = CRGB::Green;
      break;
    case 12:
      state.mode = MODE_ANIM_SPOTLIGHTS_PALETTE;
      state.bpm = 2;
      state.palette = PALETTE_DEFAULT_RAINBOW;      
      break;
    case 13:
      state.mode = MODE_ANIM_POLICE;
      state.bpm = 30;
      break;





  }
}
