#define _BLE_TRACE_
#include <ArduinoBLE.h>  // v1.2.1
//#include "utility/ATT.h"

// i/O pins
#define LED_BLUE 24              // onboard led to indicate BLE status (on = LOW)

// Use "Serial" for USB, "Serial1" for 3.3V RX TX pins
#define SERIAL Serial1


#define ACTION_OFF 0
#define ACTION_ON 1
#define ACTION_BRIGHTNESS 2
#define ACTION_MODE 10
#define ACTION_BPM 11
#define ACTION_PALETTE 12
#define ACTION_TIME_FUNCTION 13
#define ACTION_SET_LED 101
#define ACTION_SET_ALL 102


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
BLECharacteristic     cSetLed          ("19B10101-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite, /*valueSize*/5, /*fixedLength*/true);
BLECharacteristic     cSetAll          ("19B10102-E8F2-517E-4F6C-D104768A1214", BLERead | BLEWrite, 3*NUM_LEDS); // max 512



void setupCharacteristics(){
  bleService.addCharacteristic(cOnOff);               cOnOff.setEventHandler(BLEWritten, switchOnOffCallback);
  bleService.addCharacteristic(cBrightness);     cBrightness.setEventHandler(BLEWritten, actionCallback<ACTION_BRIGHTNESS>);
  bleService.addCharacteristic(cBpm);                   cBpm.setEventHandler(BLEWritten, actionCallback<ACTION_BPM>);
  bleService.addCharacteristic(cMode);                 cMode.setEventHandler(BLEWritten, actionCallback<ACTION_MODE>);
  bleService.addCharacteristic(cPalette);           cPalette.setEventHandler(BLEWritten, actionCallback<ACTION_PALETTE>);
  bleService.addCharacteristic(cTimeFunction); cTimeFunction.setEventHandler(BLEWritten, actionCallback<ACTION_TIME_FUNCTION>);
  bleService.addCharacteristic(cSetLed);             cSetLed.setEventHandler(BLEWritten, actionCallback<ACTION_SET_LED>);
  bleService.addCharacteristic(cSetAll);             cSetAll.setEventHandler(BLEWritten, actionCallback<ACTION_SET_ALL>);
}



void setup() {
  SERIAL.begin(9600);

  // set pin modes
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  
  // begin initialization
  if (!BLE.begin()) {
    while (1){
      // rapid flashing indicates error
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
  //Serial.print("Connected event, central: ");
  //Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  // central disconnected event handler
  BLE.advertise();
  //Serial.print("Disconnected event, central: ");
  //Serial.println(central.address());
}



/**
 * Communication with sphere2lamp
 */
void send_action(uint8_t action, uint8_t* data = NULL, size_t len = 0){
  SERIAL.write(action);
  uint8_t answer;
  if (SERIAL.readBytes(&answer, 1) == 1 && answer == action && len > 0){
    SERIAL.write(data, len);
  }
}

/**
 * Generic callback sending an action with the characteristic's value as payload
 */
template <uint8_t ACTION>
void actionCallback(BLEDevice central, BLECharacteristic characteristic) {
  send_action(ACTION, (uint8_t*) characteristic.value(), characteristic.valueSize());
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