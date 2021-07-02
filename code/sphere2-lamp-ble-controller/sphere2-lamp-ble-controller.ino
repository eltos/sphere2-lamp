#define _BLE_TRACE_
#include <ArduinoBLE.h>  // v1.2.1
//#include "utility/ATT.h"

// i/O pins
#define LED_BLUE 24              // onboard led to indicate BLE status (on = LOW)


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
/* Set led color manually. Payload: (see below) */
#define ACTION_SET_LED 101
#define ACTION_SET_ALL 102
/* Read out state */
#define ACTION_GET 250


#define NUM_LEDS    122


// BLE Sphere² Lamp service and chracteristics
#define BLE_NAME "Sphere² Lamp"
BLEService bleService                  ("19B10000-E8F2-517E-4F6C-D104768A1214"); 
BLEByteCharacteristic cOnOff           ("19B10001-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite); // BLEAuth
BLEByteCharacteristic cBrightness      ("19B10002-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEByteCharacteristic cMode            ("19B10010-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEByteCharacteristic cBpm             ("19B10011-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEByteCharacteristic cPalette         ("19B10012-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEByteCharacteristic cTimeFunction    ("19B10013-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLECharacteristic     cColor           ("19B10014-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite, 4, true);
BLEByteCharacteristic cLedMap          ("19B10015-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite);
BLECharacteristic     cSetLed          ("19B10101-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite, /*valueSize*/5, /*fixedLength*/true);
BLECharacteristic     cSetAll          ("19B10102-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite, 3*NUM_LEDS); // max 512



void setupCharacteristics(){
  bleService.addCharacteristic(cOnOff);               cOnOff.setEventHandler(BLEWritten, switchOnOffCallback);
  bleService.addCharacteristic(cBrightness);     cBrightness.setEventHandler(BLEWritten, actionCallback<ACTION_BRIGHTNESS>);
  bleService.addCharacteristic(cBpm);                   cBpm.setEventHandler(BLEWritten, actionCallback<ACTION_BPM>);
  bleService.addCharacteristic(cMode);                 cMode.setEventHandler(BLEWritten, actionCallback<ACTION_MODE>);
  bleService.addCharacteristic(cPalette);           cPalette.setEventHandler(BLEWritten, actionCallback<ACTION_PALETTE>);
  bleService.addCharacteristic(cTimeFunction); cTimeFunction.setEventHandler(BLEWritten, actionCallback<ACTION_TIME_FUNCTION>);
  bleService.addCharacteristic(cColor);               cColor.setEventHandler(BLEWritten, actionCallback<ACTION_COLOR>);
  bleService.addCharacteristic(cLedMap);             cLedMap.setEventHandler(BLEWritten, actionCallback<ACTION_LED_MAP>);
  bleService.addCharacteristic(cSetLed);             cSetLed.setEventHandler(BLEWritten, actionCallback<ACTION_SET_LED>);
  bleService.addCharacteristic(cSetAll);             cSetAll.setEventHandler(BLEWritten, actionCallback<ACTION_SET_ALL>);

}

void readAllCharacteristics(){
  // read out initial values
  queryCharacteristic<ACTION_ON>(cOnOff);
  queryCharacteristic<ACTION_BRIGHTNESS>(cBrightness);
  queryCharacteristic<ACTION_BPM>(cBpm);
  queryCharacteristic<ACTION_MODE>(cMode);
  queryCharacteristic<ACTION_PALETTE>(cPalette);
  queryCharacteristic<ACTION_TIME_FUNCTION>(cTimeFunction);
  queryCharacteristic<ACTION_COLOR>(cColor);
  queryCharacteristic<ACTION_LED_MAP>(cLedMap);
  
}



void setup() {
  Serial1.begin(9600);

  // set pin modes
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);

  // initialization
  if (!BLE.begin()) {
    while (1){ // rapid flashing indicates error
      digitalWrite(LED_BLUE, millis()%100 > 40 ? LOW : HIGH);
    }
  }


  // set up characteristics
  setupCharacteristics();

  
  // set up BLE service
  BLE.addService(bleService);
  BLE.setAdvertisedService(bleService);
  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);
  

  // start advertising
  BLE.setLocalName(BLE_NAME);
  BLE.advertise();

  // read initial state
  delay(3000);
  readAllCharacteristics();

}

void loop() {
  
  // poll for BLE events
  BLE.poll();

  // make blue led blink while waiting for connections
  // and light while connected (ON = LOW)
  digitalWrite(LED_BLUE, BLE.central() || millis()%1000 > 500 ? LOW : HIGH);

  delay(10);

  //int e = ATT.getPeerEncryption(ATT.getPeerEncrptingConnectionHandle());
  //Serial.print("--> Encryption: ");Serial.println(e);
}


void blePeripheralConnectHandler(BLEDevice central) {
  // central connected event handler
  BLE.stopAdvertise();
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  // central disconnected event handler
  BLE.advertise();
}



/**
 * Communication with sphere2lamp
 */
void send_action(uint8_t action, uint8_t* data = NULL, size_t len = 0){
  Serial1.write(action);
  uint8_t answer;
  if (Serial1.readBytes(&answer, 1) == 1 && answer == action && len > 0){
    Serial1.write(data, len);
  }
}

void query_action(uint8_t action, uint8_t* data, size_t len){
  send_action(ACTION_GET, &action, 1);
  Serial1.readBytes(data, len);
}

/**
 * Generic callback sending an action with the characteristic's value as payload
 */
template <uint8_t ACTION>
void actionCallback(BLEDevice central, BLECharacteristic characteristic) {
  send_action(ACTION, (uint8_t*) characteristic.value(), characteristic.valueLength());
}

/**
 * Generic method reading an action and setting the characteristic's value
 */
template <uint8_t ACTION>
void queryCharacteristic(BLECharacteristic characteristic) {
  query_action(ACTION, (uint8_t*) characteristic.value(), characteristic.valueSize());
}

/**
 * Callback for ON/OFF actions
 */
void switchOnOffCallback(BLEDevice central, BLECharacteristic characteristic) {
  if (cOnOff.value()) {
    send_action(ACTION_ON);
    digitalWrite(LED_BUILTIN, HIGH);
  } else {
    send_action(ACTION_OFF);
    digitalWrite(LED_BUILTIN, LOW);
  }
}
