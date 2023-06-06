/**
 * How to run on mac:
 * 1. ln -s -f /usr/local/bin/python3 /usr/local/bin/python
 * 2. open /Applications/Arduino.app
 */

#define DEBUG true
#define LED_ENABLED false
#define THREAD_DELAY 50
#define PIN_LEDATOM 27
#define PIN_PIR 32

#if LED_ENABLED
  #include <M5Atom.h>
#endif

bool deviceConnected = false;

// movementIndex is used to avoid duplicated data
int movementIndex = 0;

void setup() {
  #if LED_ENABLED
    M5.begin(true, false, true);
  #endif
  pinMode(PIN_PIR, INPUT);
  startBluetooth();
}

void loop() {
  if (deviceConnected) {
    // Detected movement
    if (digitalRead(PIN_PIR) == 1) {
      #if LED_ENABLED
        // Notify with Blue that movement was occurred
        M5.dis.drawpix(0, 0x0000FF);
        M5.update();
        delay(500);
        M5.dis.drawpix(0, 0x00FF00);
        M5.update();
      #endif
  
      #if DEBUG
        Serial.println("Movement occured!");
      #endif
      // Increase and write detected movement index
      movementIndex++;
      writeData(String(movementIndex));

      delay(5000);
    }
    
    #if LED_ENABLED
      // Notify with Green that device is connected
      M5.dis.drawpix(0, 0x00FF00);
      M5.update();
    #endif
  } else {
    #if LED_ENABLED
      // Notify with Red color when device is disconnected
      M5.dis.drawpix(0, 0xFF0000);
      M5.update();
    #endif
  }
  
  delay(THREAD_DELAY);
}
