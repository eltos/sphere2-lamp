
/*************
 * MAIN LOOP *
 *************/




// Color palettes (16 colors indexed as 0..255, must include both endpoints)
//   pre-defined palettes (http://fastled.io/docs/3.1/group___colorpalletes.html):
//     RainbowColors_p, RainbowStripeColors_p, OceanColors_p, CloudColors_p, LavaColors_p, ForestColors_p, PartyColors_p

DEFINE_GRADIENT_PALETTE( red_stripe_p ) {
    0,   0,  0,  0,   //black
  108,   0,  0,  0,   //black
  128, 255,  0,  0,   //red
  148,   0,  0,  0,   //black
  255,   0,  0,  0};  //black

DEFINE_GRADIENT_PALETTE( four_p ) {
    0, 255,  0,  0,   //
   63,   0,  0,  0,   //
   64, 255,  0,  0,   //
  127,   0,  0,  0,   //
  128, 255,  0,  0,   //
  191,   0,  0,  0,   //
  192, 255,255,255,   //
  255,   0,  0,  0};  //black

DEFINE_GRADIENT_PALETTE( dark_ocean_p ) {
    0,   9,  95, 184,
   55,  58, 114, 189,
  105,  12, 178, 183,
  140,  24, 110,  95,
  190,  35,  34, 188,
  230,  24, 102, 220,
  255,   9,  95, 184};
  

// pre-defined palettes (http://fastled.io/docs/3.1/group___colorpalletes.html):
// RainbowColors_p, RainbowStripeColors_p, OceanColors_p, CloudColors_p, LavaColors_p, ForestColors_p, PartyColors_p
CRGBPalette16 red_stripe = red_stripe_p;
CRGBPalette16 four_cols = four_p;
CRGBPalette16 stripes_palette = RainbowStripeColors_p;
CRGBPalette16 twinkle_palette = PartyColors_p;

// use prime numbers for BPM to reduce pattern repetition
#define THETA_BPM 9
#define PHI_BPM 5


/* 
 *  Main loop
 *  
 *  A number of predefined animation functions can be found in the anim file.
 *  Some usage examples are shown below.
 */
void loop(){

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
  anim_distance(TRIANGULAR(THETA_BPM), SAWTOOTH(PHI_BPM) + 128*(beat8(THETA_BPM) < 128), 17, &defaultPalette);
//  anim_distance(0, 0, 10);
//  anim_distance(TRIANGULAR(THETA_BPM), SAWTOOTH(PHI_BPM) + 128*(beat8(THETA_BPM) < 128), 47, &red_stripe, QUADWAVE);
//  anim_azimuth();
//  anim_azimuth(60, &twinkle_palette, CRGB::Black, QUADWAVE);

  /* Revolving alarm light / radar / searchlight */
//  anim_searchlight(HUE_RED, 60);
//  anim_searchlight(20, 60); // orange

  /* Random sprinkles and twinkles */
//  anim_twinkle(3, &twinkle_palette);
//  FastLED.clear(); anim_add_flashes();
//  FastLED.clear(); anim_add_flashes<20>(CRGB::Red, 2, 500, 1);
//  fadeToBlackBy(leds, NUM_LEDS, 10); int pos = random16(NUM_LEDS); leds[pos] += CHSV(HUE_GREEN+random8(64), 200, 255);

  /* Globe */
//  fill_map(MAP_EARTH, MAP_EARTH_COLORS); 
//  fill_map(MAP_EARTH, MAP_EARTH_COLORS); anim_searchlight(0, 20, 0, 0, 20, 230, 10, 0, true);
  



  FastLED.show();

  serial_print_fps();
}
