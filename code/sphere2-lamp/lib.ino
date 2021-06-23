/**
 * Util functions for spherical and fast math
 * 
 */



/**
 * Get latitute given the polar angle theta
 * 
 * @param theta: polar angle theta in range 0 (north pole) to 255 (south pole)
 * @return: latitude in range -pi/2 (south pole) to +pi/2 (north pole)
 */
static float lat(uint8_t theta){
  return M_PI / 2 - theta * M_PI / 255;
}

/**
 * Get longitude given the azimuthal angle phi
 * 
 * @param phi: azimuthal angle phi in range 0 to 255
 * @return: longitude in range -pi to +pi
 */
static float lon(uint8_t phi){
  return phi * M_PI * 2 / 255 - M_PI;
}

/**
 * Absolute difference of two bytes
 * 
 * @param a: first byte
 * @param b: second byte
 * @return: absolute value of the difference
 */
static uint8_t absdiff8(uint8_t a, uint8_t b){
  return a > b ? a-b : b-a;
}

/**
 * Fast approximation of acos(x) with error below 7e-5
 * 
 * @param x: the argument where -1 <= x <= 1
 * @return: the arccos(x) in range 0 <= arccos(x) < pi
 */
float acosfast(float x) {  
  bool negate = x < 0;
  x = abs(x);
  float ret = -0.0187293;
  ret *= x;
  ret += 0.0742610;
  ret *= x;
  ret -= 0.2121144;
  ret *= x;
  ret += 1.5707288;
  ret *= sqrt(1.0-x);
  return negate ? 3.1415926 - ret : ret;
}

/**
 * Fast 8 bit approximation of great circle distance.
 * Calculates the shortest angle between two points on the sphere's surface.
 * See https://en.wikipedia.org/wiki/Great-circle_navigation#Course
 *  
 * @param theta1: polar angle of first point in range 0 (north pole) to 255 (south pole)
 * @param phi1: azimuthal angle of first point in range 0 to 255
 * @param theta2: polar angle of second point in range 0 (north pole) to 255 (south pole)
 * @param phi2: azimuthal angle of second point in range 0 to 255
 * @return: great circle distance in range 0 (0 rad) to 255 (pi rad)
 */
static uint8_t distance(uint8_t theta1, uint8_t phi1, uint8_t theta2, uint8_t phi2){
  float u = 1, v = 1;
  u *= cos16(128*theta1);               // sin(lat(theta1))
  u *= cos16(128*theta2);               // sin(lat(theta2))
  v *= sin16(128*theta1);               // cos(lat(theta1))
  v *= sin16(128*theta2);               // cos(lat(theta2))
  v *= cos16(256*absdiff8(phi1, phi2)); // cos(lon(abs(phi1-phi2)))
  return acosfast((u-v/32767)/1073741824)*81.2 + 0.5;
}





/**
 * Return index of LED next to given spherical coordinate
 * 
 * @param theta: polar angle of first point in range 0 (north pole) to 255 (south pole)
 * @param phi: azimuthal angle of first point in range 0 to 255
 * @return: closest LED index
 */
static int led_near(uint8_t theta, uint8_t phi){
    int b = 0;
    float d = 0, u = cos16(128*theta), v = sin16(128*theta);
    for (int i = 0; i < NUM_LEDS; ++i){
      if (absdiff8(THETA[i], theta) > 20) continue; // heuristics saving 65% computation time
      float di = u*cos16(128*THETA[i]) - v*sin16(128*THETA[i])*cos16(256*absdiff8(PHI[i], phi));
      if (di > d){
        d = di;
        b = i;
      }
    }
    return b;
}
