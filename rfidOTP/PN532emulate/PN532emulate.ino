
#include "SPI.h"
#include "PN532_SPI.h"
#include "emulatetag.h"
#include "NdefMessage.h"

PN532_SPI pn532spi(SPI, 10);
EmulateTag nfc(pn532spi);

uint8_t ndefBuf[120];
NdefMessage message;
int messageSize;
String buff = "initial";

uint8_t uid[3] = { 0x12, 0x34, 0x56 };

String LastValue;

void emit (String buff) {
  message = NdefMessage();
  message.addUriRecord(buff);
  messageSize = message.getEncodedSize();
  if (messageSize > sizeof(ndefBuf)) {
      Serial.println("ndefBuf is too small");
      while (1) { }
  }
  Serial.print("Ndef encoded message size: ");
  Serial.println(messageSize);
  message.encode(ndefBuf);
  
  // comment out this command for no ndef message
  nfc.setNdefFile(ndefBuf, messageSize);
  // uid must be 3 bytes!
  nfc.setUid(uid);
  // start emulation (blocks)
  Serial.print("emitting:");
  Serial.println(buff);
  nfc.emulate();
  Serial.print("emitted:");
  Serial.println(buff);
}

void setup()
{
  Serial.begin(115200);
  Serial.println("------- Emulate Tag --------");
  nfc.init();
}

void loop(){
     int key = 0;
     if (Serial.available() > 0) {
        buff = Serial.readString() ;
      } 
        
     emit(buff);
 
    delay(1);
}
