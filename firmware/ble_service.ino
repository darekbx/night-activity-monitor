#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define BLE_NAME "NightMonitor"
#define SERVICE_UUID           "89409171-FE10-40B7-80A3-398A8C219855"
#define CHARACTERISTIC_UUID_TX "89409171-FE10-40AA-80A3-398A8C219855"

BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;

class ServerCallbacks: public BLEServerCallbacks {
  
  void onConnect(BLEServer* pServer) {
    #if DEBUG
      Serial.println("BLE is connected");
    #endif
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
    #if DEBUG
      Serial.println("BLE is disconnected");
    #endif
    deviceConnected = false;
  }
};

void startBluetooth() {  
  BLEDevice::init(BLE_NAME);
  
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Register notify
  pTxCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
                      
  pTxCharacteristic->addDescriptor(new BLE2902());

  pService->start();
  pServer->getAdvertising()->start(); 
}

void writeData(String data) {
  #if DEBUG
    Serial.print("Write data: ");
    Serial.println(data);
  #endif
  pTxCharacteristic->setValue(std::string(data.c_str()));
  pTxCharacteristic->notify();
}
