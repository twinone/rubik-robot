use <buildvars.scad>
use <util.scad>
use <gear.scad>
use <servo_horn_enclosing.scad>

teeth = 16;
r = gear_r();

sideways_length = (56 - arm_c2c())/2;
// redefined here
function arm_length() = enclosing_dst_large() + 6;

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
start_angle = 40;
angle = 150;

module sideways(height) {
    grip_h = 13;
    rotate(-90)
    translate([0,arm_length(),0]) {
        linear_extrude(height=height)
        arm(length=sideways_length, r=arm_width()/2);
        translate([0,0,height-grip_h])
        linear_extrude(height=grip_h)
        translate([sideways_length,-arm_width()/2,0]) square([arm_width()/2,20+arm_width()/2]);
    }
}

module arm_left(height) {
    mirror([0,1,0]) {
        gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = angle, start_angle = start_angle);
        linear_extrude(height=height) {
            arm(length=arm_length(), r = arm_width()/2);
        }
        sideways(height);
    }
}

module arm_right(height) {
    difference() {
        union() {
            rotate([0,0,-360/teeth/2]) // so they're aligned
            gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = angle, start_angle = start_angle);
            linear_extrude(height=height) {
                arm(length=arm_length(), r = arm_width()/2);
                rotate(180)arm(length=enclosing_dst_small(), r=arm_width()/2);
            }
            sideways(height);
        }
        linear_extrude(height=height) {
            enclosing_centered_holes();
            circle(r=(screw_r()+tolerance())*2);
        }
    }
}


module arms(height, center, angle = 0, extrasep = 0) {
    move = center ? -arm_c2c()/2 : 0;
    translate([0,move-extrasep,0]) {
        translate([0,arm_c2c()+extrasep*2,0])
        rotate([0,0,angle])
        arm_left(height);
        rotate([0,0,-angle])
        arm_right(height);
    }
}

arms(height = 5, center=true, angle=-0);