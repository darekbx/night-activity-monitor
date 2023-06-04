#include <M5Atom.h>

#define DEBUG true
#define THREAD_DELAY 50
#define PIN_LEDATOM 27
#define PIN_PIR 32

bool deviceConnected = false;

// movementIndex is used to avoid duplicated data
int movementIndex = 0;

void setup() {
      
  M5.begin(true, false, true);
  pinMode(PIN_PIR, INPUT);
  startBluetooth();
}

void loop() {
  if (deviceConnected) {
    // Detected movement
    if (digitalRead(PIN_PIR) == 1) {
      // Notify with Blue that movement was occurred
      M5.dis.drawpix(0, 0x0000FF);
      M5.update();
      delay(500);
      M5.dis.drawpix(0, 0x00FF00);
      M5.update();
  
      Serial.println("Movement occured!");

      // Increase and write detected movement index
      movementIndex++;
      writeData(String(movementIndex));

      delay(5000);
    }
    
    // Notify with Green that device is connected
    M5.dis.drawpix(0, 0x00FF00);
  } else {
    // Notify with Red color when device is disconnected
    M5.dis.drawpix(0, 0xFF0000);
  }
  
  delay(THREAD_DELAY);
  M5.update();
}
