#include <Servo.h>

// MOTOR PARAMS

int servo_pins [8] = {
  5,6,7,8,9,10,11,12
};

// PROTOCOL

#define REQUEST_NOOP 0x00
#define REQUEST_RESUME 0x01
#define REQUEST_DELAY 0x02
#define REQUEST_BUFFER 0x03

#define REQUEST_CALIBRATE 0x10
#define REQUEST_DETACH 0x11
#define REQUEST_WRITE 0x12

#define RESPONSE_OK 0x00
#define RESPONSE_INVALID_COMMAND 0x01
#define RESPONSE_INVALID_ARGUMENT 0x02

// CODE

Servo servos [8];
boolean servos_attached [8];

boolean processingRequests = true;

void setup() {
  pinMode(13, OUTPUT); //FIXME
  Serial.begin(9600);// 115200);
}

void processRequest() {
  int length = Serial.read();
  if (length == -1) return;
  char data [length];
  while (Serial.available() < length);
  if (Serial.readBytes(data, length) < length || length < 1) return;
  
  char id = data[0];
  if (!processingRequests && id != REQUEST_RESUME) return;
  switch (id) {
    case REQUEST_NOOP:
      requestNoop(data, length);
      break;
    case REQUEST_RESUME:
      requestResume(data, length);
      break;
    case REQUEST_DELAY:
      requestDelay(data, length);
      break;
    case REQUEST_BUFFER:
      requestBuffer(data, length);
      break;
    case REQUEST_DETACH:
      requestDetach(data, length);
      break;
    case REQUEST_WRITE:
      requestWrite(data, length);
      break;
    default:
      emitResponseSimple(RESPONSE_INVALID_COMMAND);
      processingRequests = false;
  }
}

void emitResponse(char *data, int length) {
  Serial.write(length);
  Serial.write((unsigned char*)data, length);
}

void emitResponseSimple(char id) {
  char data [1] = { id };
  emitResponse(data, 1);
}

void requestNoop(char *data, int length) {
  if (length != 1) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  emitResponseSimple(RESPONSE_OK);
}

void requestResume(char *data, int length) {
  if (length != 1) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  processingRequests = true;
  emitResponseSimple(RESPONSE_OK);
}

void requestDelay(char *data, int length) {
  if (length != 3) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  int time = ((unsigned char*)data)[1] << 8;
  time |= ((unsigned char*)data)[2];
  delay(time);
  emitResponseSimple(RESPONSE_OK);
}

void requestBuffer(char *data, int length) {
  if (length != 2) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  unsigned char peek = ((unsigned char *)data)[0];
  while (Serial.available() < peek);
  emitResponseSimple(RESPONSE_OK);
}

void requestDetach(char *data, int length) {
  if (length != 2) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  int motor = data[1];
  if (motor < 0 || motor >= 8) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  if (servos_attached[motor]) {
    servos[motor].detach();
    servos_attached[motor] = false;
  }
  emitResponseSimple(RESPONSE_OK);
}

void requestWrite(char *data, int length) {
  if (length != 3) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  int motor = data[1], pos = ((unsigned char *)data)[2];
  if (motor < 0 || motor >= 8 || pos < 0 || pos > 180) {
    emitResponseSimple(RESPONSE_INVALID_ARGUMENT);
    processingRequests = false;
    return;
  }
  if (!servos_attached[motor]) {
    servos[motor].attach(servo_pins[motor]);
    servos_attached[motor] = true;
  }
  servos[motor].write(pos);
  emitResponseSimple(RESPONSE_OK);
}

void loop() {
  while (true) processRequest();
}
