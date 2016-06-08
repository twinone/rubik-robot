use <buildvars.scad>
use <gripper.scad>
use <util/servo_enclosing.scad>
use <util/gear.scad>

servo_gear_angle = 70;
module servo_gear() {
    rotate([angle+90,0,0])
    rotate([0,90,0]) {
    rotate(360/30/2)
    big_remove_enclosing(h=gear_h)
    gear(r=servo_gear_r(), teeth=15*servo_gear_f(), h = gear_h,teeth_h=1/servo_gear_f(),round=gear_round());
    }
}

gear_h = 7;
servo_gear_angle = 70;
module turner_gear() {
    rotate(360/30/2)
    difference() {
        gear(r=turn_gear_r(), teeth=15*turn_gear_f(), h = gear_h,teeth_h=1/turn_gear_f(),round=gear_round());
        turner_screws(gear_h*2, $fn=20);
    }
}

module print_gears() {
    rotate([0,-90,0])
    servo_gear();
    translate([50,0,0])
    turner_gear();
}
