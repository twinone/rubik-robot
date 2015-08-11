#include <Servo.h>
// Default (min, max) = [544, 2400]
byte S_RB_PIN = 14;
Servo S_RB;

void setup() {
 S_RB.attach(S_RB_PIN);
 S_RB.setMaximumPulse(2500);
 Serial.begin(9600);
}

void loop() {
  // sample code
  static int v = 0;
  if (Serial.available()) {
    char ch = Serial.read();
    switch(ch) {
      case '0'...'9':
        v = v * 10 + ch - '0';
        break;
      case 's':
        S_RB.write(v);
        v = 0;
        Serial.println("Ok");
        break;
      case 'd':
        S_RB.detach();
        break;
      case 'a':
        S_RB.attach(14);
        break;
    }
  }
  Servo::refresh();
  delay(10);
} 
