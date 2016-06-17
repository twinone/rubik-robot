# Building
This file contains the steps needed to build the robot

# The parts

* 3D print the following parts

![overview](https://raw.githubusercontent.com/twinone/rubik-robot/master/scad/overview.png)

Amount | Piece | 1 Piece time | Total time | Support
--- | --- | --- | --- | ---
4x | [arm_left](https://github.com/twinone/rubik-robot/blob/master/scad/build/arm_left.stl) | 00:25 | 01:40 | no
4x | [arm_right](https://github.com/twinone/rubik-robot/blob/master/scad/build/arm_right.stl) | 00:25 | 01:40 | **yes**
4x | [grip_top](https://github.com/twinone/rubik-robot/blob/master/scad/build/grip_top.stl) | 00:40 | 02:40 | no
4x | [grip_bottom](https://github.com/twinone/rubik-robot/blob/master/scad/build/grip_bottom.stl) | 00:35 | 02:20 | no
4x | [grip_pad](https://github.com/twinone/rubik-robot/blob/master/scad/build/grip_pad.stl) | 00:05 | 00:20 | no
4x | [grip_cylinder](https://github.com/twinone/rubik-robot/blob/master/scad/build/grip_cylinder.stl) | 00:45 | 03:00 | no
4x | [holder_top](https://github.com/twinone/rubik-robot/blob/master/scad/build/holder_top.stl) | 00:45 | 03:00 | **yes**
4x | [holder_bottom](https://github.com/twinone/rubik-robot/blob/master/scad/build/holder_bottom.stl) | 01:30 | 06:00 | **yes**
4x | [servo_gear](https://github.com/twinone/rubik-robot/blob/master/scad/build/servo_gear.stl) | 00:20 | 01:20 | no
4x | [grip_gear](https://github.com/twinone/rubik-robot/blob/master/scad/build/grip_gear.stl) | 00:20 | 01:20 | no
8x | [foot](https://github.com/twinone/rubik-robot/blob/master/scad/build/foot.stl) | 01:00 | 04:00 | **yes**

Total printing time: 27h (measured for a BQ Prusa i3 Hephestos at medium quality)

You can print them all using a 15-20% infill and walls of 1mm. Layer height of up to 0.3mm is fine. It's recommended to print parts one by one, especially the bigger ones, so you don't have to start again more than one piece when one of them fails.


* Non-printable hardware

Amount | Type | Unit price | Total price
--- | --- | --- | ---
4x | [Tower Pro MG90S micro servo](http://www.aliexpress.com/item/New-MG90S-Gear-Metal-Servo-Micro-Servo-For-Boat-Car-Plane-Helicopter/32627521737.html) | $2.74 | $10.96
4x | [Tower Pro MG995 servo](http://www.aliexpress.com/item/Towerpro-Servos-Digital-MG995-Servo-Metal-Gear-for-Arduino-Board-DIY-Smart-Vehicle-Helicopter-Airplane-Aeroplane/32465724289.html?spm=2114.01010208.3.20.8k8Rny&ws_ab_test=searchweb201556_0,searchweb201602_1_10037_10017_507_10032_401,searchweb201603_6&btsid=a9de8b50-e2ea-4e2f-8265-d85bdd6a58ad) | $3.86 | $15.44
1x | [Arduino Nano V3.0](http://www.aliexpress.com/item/Nano-CH340-ATmega328P-MicroUSB-Compatible-for-Arduino-Nano-V3/32572612009.html?spm=2114.01010208.3.1.kR0wPs&ws_ab_test=searchweb201556_0,searchweb201602_1_10037_10017_507_10032_401,searchweb201603_6&btsid=996c5237-8b2a-4063-bbd8-3e84b59453f0) | $1.88 | $1.88
1x | [HC-06 Bluetooth Module](http://www.aliexpress.com/item/Free-shipping-HC06-HC-06-Wireless-Serial-4-Pin-Bluetooth-RF-Transceiver-Module-RS232-TTL-for/32446248487.html?spm=2114.01010208.3.1.6oaKXn&ws_ab_test=searchweb201556_0,searchweb201602_1_10037_10017_507_10032_401,searchweb201603_6&btsid=b3f8c97e-f215-4245-b35a-6c0416e155e0) | $2.75 | $2.75
1x | [Breadboard](http://www.aliexpress.com/item/1pcs-Quality-mini-bread-board-breadboard-8-5CM-x-5-5CM-400-holes-For-expansion-arduino/1906352269.html) | $1.07 | $1.07
1x | [Jumper wires](http://www.aliexpress.com/item/120pcs-20cm-male-male-male-female-and-female-jumper-wire-Dupont-cable-for-Arduino/1728903423.html) | $2.65 | $2.65

Total price: $34.75 (€30.68)
 

