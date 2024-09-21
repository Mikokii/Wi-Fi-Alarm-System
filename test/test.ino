#ifdef ESP8266
 #include <ESP8266WiFi.h>
 #else
 #include <WiFi.h>
#endif

#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>

/*** Push Buttons ***/
const int buttonLeft = 12;
const int buttonRight = 13;
int count = 0;

/**** Movement Detector Settings *****/
const int sensorMovement = 0; 
int movement;
int previousMovement = HIGH;

/**** Buzzer Settings *****/
const int buzzer = 14;
int buzzerControl = HIGH; //for adjustement depending on the message

/**** LED Pins *******/
const int ledSensor = 5; //Set LED pin as GPIO5
const int ledActive = 4; // Set LED pin as GPIO4

/**** Activating Alarm System ****/
int starter = 0;
int startSent = 0;

/*** MQTT Messages Topics ***/
const char* STARTING = "starting";
const char* MOVEMENT = "movement";
const char* DEVICE_ID = "deviceID";
const char* SOUND = "sound";

/*** MQTT Recieved Messages ***/
char receivedMessage;
String incomingMessage = "";

/*** MQTT Publishing Messages ***/
DynamicJsonDocument doc(1024);
char mqtt_message[128];

/******* LWT Settings *******/
const char* LWT_TOPIC = "ON";
const char* LWT_MESSAGE = "{\"deviceID\":\"NodeMCU\",\"ON\":0}";

/****** WiFi Connection Details *******/
const char* ssid = "";
const char* password = "";

/******* MQTT Broker Connection Details *******/
const char* mqtt_server = "";
const char* mqtt_username = "";
const char* mqtt_password = "";
const int mqtt_port = 8883;

/**** Secure WiFi Connectivity Initialisation *****/
WiFiClientSecure espClient;

/**** MQTT Client Initialisation Using WiFi Connection *****/
PubSubClient client(espClient);

unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE (50)
char msg[MSG_BUFFER_SIZE];

/****** Root Certificate *********/

static const char *root_ca PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC
B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv
KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn
OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn
jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw
qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI
rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq
4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA
mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d
emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
-----END CERTIFICATE-----
)EOF";

void setup_wifi();
void reconnect();
void callback(char* topic, byte* payload, unsigned int length);
void publishMessage(const char* topic, String payload , boolean retained);
void movementReact();
void startMovementDetector();
void alarmNotActivated();
void alarmActivated();
void handleONMessage();
void startingAlarmSystemMessage();

/**** Application Initialisation Function******/
void setup() {

  pinMode(buttonLeft, INPUT_PULLUP);
  pinMode(buttonRight, INPUT_PULLUP);
  pinMode(ledSensor, OUTPUT); //set up LED
  pinMode(ledActive, OUTPUT); //set up LED
  pinMode(buzzer, OUTPUT);
  digitalWrite(ledActive, HIGH);
  pinMode(sensorMovement, INPUT); // set up sensor
  Serial.begin(9600);
  while (!Serial) delay(1);
  setup_wifi();

  #ifdef ESP8266
    espClient.setInsecure();
  #else
    espClient.setCACert(root_ca); 
  #endif

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

/******** Main Function *************/
void loop() {

  if (count == 500000) {
    if (!client.connected()) reconnect(); // checking if client is connected
    client.loop();

    movementReact();  // react to movement
    handleONMessage();
    handlePoweringUpSystem();

    previousMovement = movement;
    count = 0;
  }

  if(digitalRead(buttonLeft) == LOW || digitalRead(buttonRight) == LOW) {
    digitalWrite(ledSensor, HIGH);
  } else {
    digitalWrite(ledSensor, LOW);
  }

  count++;
}

/************* Connect to WiFi ***********/
void setup_wifi() {

  delay(10);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
  randomSeed(micros());

}

/************* Connect to MQTT Broker ***********/
void reconnect() {
  while (!client.connected()) {

    Serial.print("Attempting MQTT connection...");
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    
    if (client.connect(clientId.c_str(), mqtt_username, mqtt_password, LWT_TOPIC, 1, true, LWT_MESSAGE)) {
      Serial.println("connected");

      client.subscribe(SOUND); 
      defaultSoundMessage();
      defaultStartMessage();

    } else {
      Serial.print(client.state());
      delay(5000);
    }
  }
}

/***** Callback Method For Receiving MQTT Messages And Sound Control ****/

void callback(char* topic, byte* payload, unsigned int length) {

  for (int i = 0; i < length; i++) incomingMessage+=(char)payload[i];

  if (incomingMessage.length() >=2) {
    receivedMessage = incomingMessage[incomingMessage.length() - 2];
    if( strcmp(topic, SOUND) == 0){
        if (receivedMessage == '1') buzzerControl = HIGH;   // Turn the buzzer on
        else buzzerControl = LOW;                           // Turn the buzzer off
    }
  } else {
    Serial.println("ERROR. Wrong type of message received");
  }
}

/**** Method for Publishing MQTT Messages **********/
void publishMessage(const char* topic, String payload , boolean retained){
  client.publish(topic, payload.c_str(), true);
}

/**** Method For LED and Buzzer Reaction To Movement *****/
void movementReact() {
  movement = digitalRead(sensorMovement);
  if (movement == HIGH && starter == 0 && previousMovement == HIGH) {
    startMovementDetector();
  } else if (movement == HIGH && starter == 1) {
    alarmActivated();
  } else if (previousMovement == HIGH && movement == LOW && starter == 0) {
    starter = 1;
  } else if (movement == LOW && starter == 1) {
    alarmNotActivated();
  }
}

/*** Method For Starting The Alarm System - Buzzer and LED Reaction ***/
void startMovementDetector() {
  digitalWrite(buzzer, LOW);
  for (int i = 0; i < 10; i++) {
    digitalWrite(ledSensor, HIGH);
    delay(100);
    digitalWrite(ledSensor, LOW);
    delay(100);
  }
}

/*** Method For No Movement Detected - Buzzer and LED Reaction ***/
void alarmNotActivated() {
  digitalWrite(buzzer, LOW);
  digitalWrite(ledSensor, LOW);
}

/*** Method For Activating The Alarm System - Buzzer and LED Reaction (After Start Mode) ***/
void alarmActivated() {
  for (int i = 0; i < 10; i++){
    digitalWrite(ledSensor, HIGH);
    digitalWrite(buzzer, buzzerControl);
    delay(100);
    digitalWrite(ledSensor, LOW);
    digitalWrite(buzzer, LOW);
    delay(100);
  }
}

/*** Method For Sending ON Message TO MQTT - Device Is ON ***/
void handleONMessage() {
  doc[DEVICE_ID] = "NodeMCU";
  doc[LWT_TOPIC] = 1;
  serializeJson(doc, mqtt_message);
  publishMessage(LWT_TOPIC, mqtt_message, true);
  doc.remove(DEVICE_ID);
  doc.remove(LWT_TOPIC);
}

/*** Method for Publishing Message STARTING 0 When The Alarm System Is Beginning To Work ***/
void alarmSystemMessageAfterStart() { 
  doc[DEVICE_ID] = "NodeMCU";
  doc[STARTING] = 0;

  serializeJson(doc, mqtt_message);
  publishMessage(STARTING, mqtt_message, true);
  doc.remove(STARTING);
  doc.remove(DEVICE_ID);
  startSent = 1;
}

/*** Method for when alarm system is already turned on ***/
void alarmSystemStart() {
  doc[DEVICE_ID] = "NodeMCU";
  doc[STARTING] = 1;

  serializeJson(doc, mqtt_message);
  publishMessage(STARTING, mqtt_message, true);
  doc.remove(STARTING);
  doc.remove(DEVICE_ID);
}

/*** Handle Powering Up System And Messages Management ***/
void handlePoweringUpSystem() {
  if(starter) {
    if (!startSent){
      alarmSystemMessageAfterStart();     //finished powering up, one message starting 0 confirms it
    }
    movementMessagePublished();           //when system is on and working and movement messages get published
  } else {
    alarmSystemStart();                   //when system is in the process of turning on a message starting 1 is published
  }
}

/*** Movement Message Published When Device is ON And Working ***/
void movementMessagePublished() {
  doc[DEVICE_ID] = "NodeMCU";
  doc[MOVEMENT] = movement;

  serializeJson(doc, mqtt_message);
  publishMessage(MOVEMENT, mqtt_message, false); 
  doc.remove(MOVEMENT);
  doc.remove(DEVICE_ID);
}

/*** Establish default Sound Message Settings ***/
void defaultSoundMessage() {
  doc[DEVICE_ID] = "NodeMCU";
  doc[SOUND] = 1;

  serializeJson(doc, mqtt_message);
  publishMessage(SOUND, mqtt_message, true);
  doc.remove(SOUND);
  doc.remove(DEVICE_ID);
}

/*** Establish Default Start Settings Message ***/
void defaultStartMessage() {
  doc[DEVICE_ID] = "NodeMCU";
  doc[STARTING] = 1;

  serializeJson(doc, mqtt_message);
  publishMessage(STARTING, mqtt_message, true);
  doc.remove(STARTING);
  doc.remove(DEVICE_ID);
}