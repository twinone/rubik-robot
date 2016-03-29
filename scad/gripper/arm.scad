use <buildvars.scad>
use <util.scad>
use <gear.scad>
use <servo_horn_enclosing.scad>

teeth = 16;
r = gear_r();


// length: center to center length
module arm(length, r=arm_width()/2, center = false) {
    move = center ? -length/2 : 0;
    translate([move,0,0])
    difference() {
        hull() {
            circle(r=r);
            translate([length,0,0])
            circle(r=r);
        }
        screw_hole();
        translate([length,0,0]) screw_hole();
    }
}

module arm_left(height) {
    mirror([0,1,0]) {
        gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = 180);
        linear_extrude(height=height)
        arm(length=arm_length(), r = arm_width()/2);
    }
}

module arm_right(height) {
    difference() {
        union() {
            rotate([0,0,360/teeth/2]) // so they're aligned
            gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = 180);
            linear_extrude(height=height)
            arm(length=arm_length(), r = arm_width()/2);
        }
        linear_extrude(height=height) {
            enclosing_centered_holes();
            circle(r=(screw_r()+tolerance())*2);
        }
    }
}

module arms(height, center) {
    move = center ? -arm_c2c()/2 : 0;
    translate([0,move,0]) {
        translate([0,arm_c2c(),0])
        arm_left(height);
        arm_right(height);
    }
}

arms(height = 5, center=true);