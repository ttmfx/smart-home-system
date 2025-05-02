import os
import glob
import time
import RPi.GPIO as GPIO
from gpiozero import DistanceSensor, LightSensor, AngularServo
import asyncio
import websockets
import json


GPIO.setmode(GPIO.BCM)


# PINS Definition and Setup
FAN_PIN = 17 # 26
HEATER_PIN = 13
TRIGGER_PIN = 22
ECHO_PIN = 26
LIGHT_SENSOR_PIN = 9
BULB_PIN = 11
FIRE_PIN = 5
INDOOR_BULB_PIN = 19
BUZZER_PIN = 27
SERVO_PIN = 18
GPIO.setup(FAN_PIN, GPIO.OUT) # Set GPIO27 as an output
GPIO.setup(HEATER_PIN, GPIO.OUT) # Set GPIO27 as an output
GPIO.setup(HEATER_PIN, GPIO.OUT)  
GPIO.setup(LIGHT_SENSOR_PIN, GPIO.IN)
GPIO.setup(BULB_PIN, GPIO.OUT)
GPIO.setup(INDOOR_BULB_PIN, GPIO.OUT)
GPIO.setup(FIRE_PIN, GPIO.IN)
GPIO.setup(BUZZER_PIN, GPIO.OUT)
GPIO.setup(SERVO_PIN, GPIO.OUT)

ultrasonic = DistanceSensor(echo=ECHO_PIN, trigger=TRIGGER_PIN, threshold_distance=0.3)

#setting up 1-wire connection for temperature sensor
os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')
device_file = "/sys/bus/w1/devices/28-031097798de2/w1_slave"


 
INTRUDER_FLAG = 0;
INTRUDER_DETECTION_FLAG = False;
AUTOMATIC_FLAG = 1;

temperature = -1
lights_on = False
intruder_detected = False
MIN_TEMP = 25
MAX_TEMP = 28

connected_clients = set()
def set_min_temperature(input_string):
    global MIN_TEMP
    
    parts = input_string.split(',')

# Get the number after the comma (second part of the split string)
    MIN_TEMP  = (int) (parts[1] if len(parts) > 1 else 25)
    print("min temp" +  str( MIN_TEMP))
    

def set_max_temperature(input_string):
    global MAX_TEMP
    parts = input_string.split(',')
# Get the number after the comma (second part of the split string)
    MAX_TEMP  = (int) (parts[1] if len(parts) > 1 else 28)
    print("max temp" + str(MAX_TEMP))


def read_adc(channel):
    adc = spi.xfer2([1, (8 + channel) << 4, 0])
    data = ((adc[1] & 3) << 8) + adc[2]  # 10-bit value (0-1023)
    return data
    
# WebSocket server handler
websocket_ = 0

pwm = GPIO.PWM(SERVO_PIN, 50)
pwm.start(0)

def set_angle(angle):
    duty = 2 + (angle / 18)
    GPIO.output(SERVO_PIN, True)
    pwm.ChangeDutyCycle(duty)
    time.sleep(0.5)
    GPIO.output(SERVO_PIN, False)
    pwm.ChangeDutyCycle(0)
   # pwm.stop()
    # GPIO.cleanup()

def open_door():
    set_angle(90)
    print("Door opened")
    
def close_door():
    set_angle(0)
    print("Door closed")


def stop_alert():
    GPIO.output(BUZZER_PIN, GPIO.LOW)
    print("stop_alert")
    
    
async def handle_client(websocket):
    global AUTOMATIC_FLAG
    global INTRUDER_DETECTION_FLAG
    global MIN_TEMP
    global MAX_TEMP
    connected_clients.add(websocket)
    websocket_ = websocket
    print("Client connected")
    try:
        async for message in websocket:
            print(f"Received from client: {message}")
            response = f"Echo: {message}"
            if message == "fan_on":
                GPIO.output(FAN_PIN, GPIO.HIGH)
                
            elif message == "fan_off":
                GPIO.output(FAN_PIN, GPIO.LOW)
                
            elif message == "heater_on":
                GPIO.output(HEATER_PIN, GPIO.HIGH)
               
            elif message == "heater_off":
                GPIO.output(HEATER_PIN, GPIO.LOW)
              
            elif message == "outdoor_bulb_on":
                GPIO.output(BULB_PIN, GPIO.HIGH)
               
            elif message == "outdoor_bulb_off":
                GPIO.output(BULB_PIN, GPIO.LOW)
            
            elif message == "indoor_bulb_on":
                GPIO.output(INDOOR_BULB_PIN, GPIO.HIGH)
                print("indoor bulb off")
            elif message == "indoor_bulb_off":
                GPIO.output(INDOOR_BULB_PIN, GPIO.LOW)
                print("indoor bulb on")
            elif message == "automatic_on":
                AUTOMATIC_FLAG = True
                print("Automatic")
            elif message == "automatic_off":
                AUTOMATIC_FLAG = False
                print("Manual") 
            elif message == "intruder_detection_on":
                INTRUDER_DETECTION_FLAG = True
                print("Intruder detection on")
            elif message == "intruder_detection_off":
                INTRUDER_DETECTION_FLAG = False
                print("Intruder detection off") 
            elif message == "door open":
                open_door()
            elif message == "door close":
                close_door()
            elif message == "stop intruder alert":
                stop_alert()
            elif message == "stop fire alert":
                stop_alert()
            elif "min temp" in message:
                set_min_temperature(message)
            elif "max temp" in message:
                set_max_temperature(message)
               
            #await websocket.send(response)
    except websockets.exceptions.ConnectionClosed:
        print("Client disconnected")

# Start WebSocket server
async def start_server():
    server = await websockets.serve(handle_client, "0.0.0.0", 8765)
    print("WebSocket server started on ws://0.0.0.0:8765")
    # await server.wait_closed()
    await asyncio.Future() 

async def send_updates():
    

    while True:
        await asyncio.sleep(0.5)  # Send updates every 5 seconds
        data = {
            "fan": "on" if GPIO.input(FAN_PIN) == 1 else "off" ,
            "heater": "on" if GPIO.input(HEATER_PIN) == 1 else "off",
            "indoor_bulb": "on" if GPIO.input(BULB_PIN) == 1 else "off" ,
            "outdoor_bulb": "on" if GPIO.input(BULB_PIN) == 1 else "off" ,
            "fire": True if GPIO.input(FIRE_PIN) == 1 else False,
            "intruder": True if INTRUDER_FLAG == 1 else False,
            "temperature": round(temperature,1)
        }
        
        json_data = json.dumps(data)
        if connected_clients:
            
            for client in connected_clients:
                try:
                    await client.send(json_data)
                except:
                    pass  # Ignore failed messages (client may have disconnected)
 
 
def turn_fan_on():
    GPIO.output(FAN_PIN, GPIO.HIGH) # Turn GPIO17 ON  
     
def turn_fan_off():
    GPIO.output(FAN_PIN, GPIO.LOW) # Turn GPIO17 OFF
     
def turn_heater_on():
    GPIO.output(HEATER_PIN, GPIO.HIGH) # Turn GPIO17 ON  
    
    
def turn_heater_off():
    GPIO.output(HEATER_PIN, GPIO.LOW) # Turn GPIO17 OFF  
    
    
def read_temp_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines
 
def read_temp():
    global temperature
    global MIN_TEMP
    global MAX_TEMP
    lines = read_temp_raw()
    while lines[0].strip()[-3:] != 'YES':
        time.sleep(0.2)
        lines = read_temp_raw()
    equals_pos = lines[1].find('t=')
    if equals_pos != -1:
        temp_string = lines[1][equals_pos+2:]
        temp_c = float(temp_string) / 1000.0
        temp_f = temp_c * 9.0 / 5.0 + 32.0
        if(AUTOMATIC_FLAG):
        
            if temp_c > MAX_TEMP:
                turn_fan_on()
                turn_heater_off()
                print("motor on")
            elif temp_c < MIN_TEMP:
                turn_heater_on()
                turn_fan_off()
                print("heater on")
            else:
                turn_fan_off()
                turn_heater_off()
                print("motor & heater off")
            
    
        temperature = temp_c
        print("Min temp: " + str(MIN_TEMP), " |  Max temp: " + str(MAX_TEMP))
        
        return temp_c, temp_f
	
	
def alert_intrusion():
    global INTRUDER_FLAG
   
    print("---------------intruder detected---------------")
    INTRUDER_FLAG = 1
    GPIO.output(BUZZER_PIN, GPIO.HIGH)
    
def no_intrusion():
    global INTRUDER_FLAG
    INTRUDER_FLAG = 0
    print("no intruder")
    # GPIO.output(BUZZER_PIN, GPIO.LOW)
    
    
  

def no_action():
    pass
def check_intrusion():
    #print("------intrusion------")
    if INTRUDER_DETECTION_FLAG:
        ultrasonic.when_in_range = alert_intrusion
        ultrasonic.when_out_of_range = no_intrusion
    else:
        ultrasonic.when_in_range = no_action
        ultrasonic.when_out_of_range = no_action
    
    
def switch_bulb_on():
    GPIO.output(BULB_PIN, GPIO.HIGH) 

def switch_bulb_off():
    GPIO.output(BULB_PIN, GPIO.LOW) 
    
def check_light_intensity():
    lightValue = GPIO.input(LIGHT_SENSOR_PIN)
    print("Light sensor value:" + str(lightValue))
    
    if(AUTOMATIC_FLAG):
        if lightValue > 0:
            switch_bulb_off()
        else:
            switch_bulb_on()
    
    



def check_fire():
    
    if GPIO.input(FIRE_PIN) == 0:
        print("No fire")
        GPIO.output(BUZZER_PIN, GPIO.LOW)
    else:
        print("Smoke/Fire detected")
        GPIO.output(BUZZER_PIN, GPIO.HIGH)

# Run the server
# asyncio.run(start_server())

async def other_task():
    while True:
        print(read_temp())	
        check_intrusion()
        check_light_intensity()
        check_fire()
        # lightValue = GPIO.input(LIGHT_SENSOR_PIN)
        # print("Light sensor value:" + str(lightSensor.value))
        await asyncio.sleep(1)
        # time.sleep(1)
    


async def main():
    """ Run WebSocket server and other tasks in parallel """
    task1 = asyncio.create_task(start_server())  # WebSocket server
    task2 = asyncio.create_task(other_task())    # Other background tasks
    task3 = asyncio.create_task(send_updates())  
   
    await asyncio.gather(task1, task2, task3)    
#asyncio.run(start_server())
#while True:
 #   print(read_temp())	
  #  check_intrusion()
  #  check_light_intensity()
  #  check_fire()
        # lightValue = GPIO.input(LIGHT_SENSOR_PIN)
        # print("Light sensor value:" + str(lightSensor.value))
    
    #time.sleep(1)

if __name__ == "__main__":
    asyncio.run(main())  


