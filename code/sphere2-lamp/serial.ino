
/********************
 * Serial Interface *
 ********************/

/* Swich lamp off. Payload: [] */
#define ACTION_OFF 0
/* Swich lamp on. Payload: [] */
#define ACTION_ON 1
/* Set overall brightness. Payload: [uint8_t brightness] */
#define ACTION_BRIGHTNESS 2
/* Set mode. Payload: [uint8_t mode_constant] (see main for list of modes) */
#define ACTION_MODE 10
/* Set animation speed in bpm. Payload: [uint8_t bpm] */
#define ACTION_BPM 11
/* Set color palette. Payload: [uint8_t palette_constant] (see main for list of palettes)*/
#define ACTION_PALETTE 12
/* Set time interpolation function. Payload: [uint8_t function_constant] (see main for list of functions) */
#define ACTION_TIME_FUNCTION 13
/* Set a color. Payload: [uint8_t 0_or_1, r_or_h, g_or_s, b_or_v] */
#define ACTION_COLOR 14
/* Set an led map. Payload: (see below) */
#define ACTION_LED_MAP 15
/* Set led color manually. Payload: [uint8_t map_constant] (see main for list of maps) */
#define ACTION_SET_LED 101
#define ACTION_SET_ALL 102
/* Read out state */
#define ACTION_GET 250






/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs. Multiple bytes may be available.

  Protocol:
  - remote sends single "action" byte and waits for answer of same byte
  - after receiving the same byte as answer, remote sends all further payload data (if any)
  This is done to prevent lost bytes, see https://github.com/FastLED/FastLED/wiki/Interrupt-problems
  and ensures that all requests are handeled as fast as possible (but not too fast, 
  and with at least one frame in between)
      
*/
uint8_t buf[10];
/* read n bytes into buffer and return true on success */
bool got(size_t n){
  return Serial.readBytes(buf, n) == n;
}
/* read a color from the buffer at the given position (+4 bytes) */
CRGB color_from_buf(size_t i){
  return buf[i] ? CHSV(buf[i+1], buf[i+2], buf[i+3]) 
                : CRGB(buf[i+1], buf[i+2], buf[i+3]);
}
  

void serialEvent() {
  
  // first byte signals action
  uint8_t action = (uint8_t) Serial.read();
  
  // answer with same byte to acknowledge
  Serial.write(action);
  
  // payload bytes (depending on the action)
  switch (action) {
    case ACTION_OFF: // OFF
      state.on = false;
      stateChanged();
      break;
    
    case ACTION_ON: // ON
      state.on = true;
      stateChanged();
      break;

    case ACTION_BRIGHTNESS: // set overall brightness
      if (got(1)){
        state.brightness = buf[0];
        stateChanged();
      } break;

    case ACTION_MODE: // set mode
      if (got(1)){
        state.mode = buf[0];
        stateChanged();
      } break;

    case ACTION_BPM: // set bpm
      if (got(1)){
        state.bpm = buf[0];
        stateChanged();
      } break;

    case ACTION_PALETTE: // set palette
      if (got(1)){
        state.palette = buf[0];
        stateChanged();
      } break;

    case ACTION_TIME_FUNCTION: // set time function
      if (got(1)){
        state.time_function = buf[0];
        stateChanged();
      } break;

    case ACTION_COLOR:
      if (got(4)){
        state.color = color_from_buf(0);
        stateChanged();
      } break;

    case ACTION_LED_MAP:
      if (got(1)){
        state.led_map = buf[0];
        stateChanged();
      } break;
      

    case ACTION_SET_LED: // set individual led colors by index
      if (got(5)){
        state.mode = MODE_MANUAL;
        CRGB color = color_from_buf(1);
        if (buf[0] < NUM_LEDS){
          leds[buf[0]] = color;
        } else if (buf[0] == 255){
          fill_solid(leds, NUM_LEDS, color);
        }
      } break;

    case ACTION_SET_ALL: // set individual led colors at once
      state.mode = MODE_MANUAL;
      for (size_t i = 0; i < NUM_LEDS; ++i){
        if (got(3)){
          leds[i] = CRGB(buf[0], buf[1], buf[2]);
        } else {
          break;
        }
      }
      break;

    case ACTION_GET: // request state
      if (got(1)){
        switch (buf[0]) {
          case ACTION_ON: case ACTION_OFF: Serial.write(state.on ? 1 : 0); break;
          case ACTION_BRIGHTNESS: Serial.write(state.brightness); break;
          case ACTION_MODE: Serial.write(state.mode); break;
          case ACTION_BPM: Serial.write(state.bpm); break;
          case ACTION_PALETTE: Serial.write(state.palette); break;
          case ACTION_TIME_FUNCTION: Serial.write(state.time_function); break;
          case ACTION_COLOR: Serial.write(0); Serial.write(state.color.r); Serial.write(state.color.g); Serial.write(state.color.b); break;
          case ACTION_LED_MAP: Serial.write(state.led_map); break;
        }
      } break;

  }
    
}





void setup_serial(){
  Serial.begin(9600);
  Serial.setTimeout(1000);
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
