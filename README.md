# rubik-robot

A low cost, 3D printable Rubik's Cube solving robot. This project was built in about two or three weeks, incuding the hardware design, software, etc. Just printing and assembling it should take about two days, so you can do it in a weekend. The total cost of the project is about 45$, from which 35$ are for the non-printable hardware, and 10$ are the actual printed parts.

![Timelapse of the robot in action](https://cloud.githubusercontent.com/assets/4309591/15910761/b8f5c6c6-2dcc-11e6-9fa7-2232c03a1e91.gif)

## Hardware used

* 3D Printer (BQ Prusa i3 Hephestos)
* An Android phone (OnePlus One)
* 4x micro servo (TowerPro MG 995)
* 4x servo (Tower Pro SG 90)
* Arduino Nano, breadboard, jumper wires, etc.
* Bluetooth module (HC-05)
* M3 screws / nuts

## Parts

 - OpenSCAD designs for the printable parts (see `scad/`).
 - The sketch that runs on the Arduino (see `arduino/`).
 - An Android application (see `android/`) that uses the camera and works
   in cooperation with the Arduino to scan and solve the cube automatically.
 - Standalone utility apps (see `utils/`) for talking to the Arduino.

## Make your own

See [`BUILDING.md`](https://github.com/twinone/rubik-robot/blob/master/BUILDING.md)

## Authors

* [@twinone](https://github.com/twinone)
* [@jmendeth](https://github.com/jmendeth)

## License

Everything is MIT-licensed (see `LICENSE`) except this README and
the documentation files under `docs`, which are under a
[Creative Commons Attribution-NonCommercial 4.0 International License](https://creativecommons.org/licenses/by-nc/4.0).
