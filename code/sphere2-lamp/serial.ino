

#define ACTION_OFF 0
#define ACTION_ON 1
#define ACTION_BRIGHTNESS 2
#define ACTION_MODE 10
#define ACTION_BPM 11
#define ACTION_PALETTE 12
#define ACTION_TIME_FUNCTION 13
#define ACTION_SET_LED 101
#define ACTION_SET_ALL 102

#define PALETTE_DEFAULT_RAINBOW 0
#define PALETTE_RED_STRIPE 1
#define PALETTE_PARTY 2

#define TIME_FUNCTION_SAWTOOTH 0
#define TIME_FUNCTION_SAWTOOTH_REVERSE 1
#define TIME_FUNCTION_TRIANGULAR 2
#define TIME_FUNCTION_SINUSOIDAL 3
#define TIME_FUNCTION_QUADWAVE 4



/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs. Multiple bytes may be available.

  Protocol:
  - remote sends single "action" byte and waits for answer of same byte
  - after receiving the same byte as answer, remote sends all further data
  This is done to prevent lost bytes, see https://github.com/FastLED/FastLED/wiki/Interrupt-problems
  and ensures that all requests are handeled as fast as possible (but not too fast, and with at least one frame in between)
      
*/
uint8_t buf[10];
bool got(size_t n){
  return Serial.readBytes(buf, n) == n;
}

void serialEvent() {
  
  // first byte signals action
  uint8_t action = (uint8_t) Serial.read();
  
  // answer with same byte to acknowledge
  Serial.write(action);
  
  // following bytes depend on the action
  switch (action) {
    case ACTION_OFF: // OFF
      state = 0;
      break;
    
    case ACTION_ON: // ON
      state = 1;
      break;

    case ACTION_BRIGHTNESS: // BRIGHTNESS
      if (got(1)){
        FastLED.setBrightness(buf[0]);
      } break;

    case ACTION_MODE: // set mode
      if (got(1)){
        mode = buf[0];
        FastLED.clear();
      } break;

    case ACTION_BPM: // set bpm
      if (got(1)){
        bpm = buf[0];
      } break;

    case ACTION_PALETTE: // set bpm
      if (got(1)){
        switch (buf[0]){
          case PALETTE_DEFAULT_RAINBOW: default:  palette = RainbowColors_p;  break;
          case PALETTE_RED_STRIPE:                palette = red_stripe_p;     break;
          case PALETTE_PARTY:                     palette = PartyColors_p;    break;
        }        
      } break;

    case ACTION_TIME_FUNCTION: // set bpm
      if (got(1)){
        switch (buf[0]){
          case TIME_FUNCTION_SAWTOOTH: default:   time_function = SAWTOOTH;           break;
          case TIME_FUNCTION_SAWTOOTH_REVERSE:    time_function = SAWTOOTH_REVERSE;   break;
          case TIME_FUNCTION_TRIANGULAR:          time_function = TRIANGULAR;         break;
          case TIME_FUNCTION_SINUSOIDAL:          time_function = SINUSOIDAL;         break;
          case TIME_FUNCTION_QUADWAVE:            time_function = QUADWAVE;           break;
        }        
      } break;

    case ACTION_SET_LED: // set individual led colors
      if (got(5)){
        CRGB color = buf[1] ? CHSV(buf[2], buf[3], buf[4]) : CRGB(buf[2], buf[3], buf[4]);
        if (buf[0] < NUM_LEDS){
          leds[buf[0]] = color;
        } else if (buf[0] == 255){
          fill_solid(leds, NUM_LEDS, color);
        }
      } break;

    case ACTION_SET_ALL:
      for (size_t i = 0; i < NUM_LEDS; ++i){
        if (got(3)){
          leds[i] = CRGB(buf[0], buf[1], buf[2]);
        } else {
          break;
        }
      }
      break;

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
