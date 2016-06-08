use <buildvars.scad>
use <util/util.scad>
use <util/gear.scad>
use <util/micro_servo_horn_enclosing.scad>

teeth = 16;
r = gear_r();


sideways_length = (56 - arm_c2c())/2;
// redefined here
function arm_length() = enclosing_dst_large() + 6;
l1 = arm_length();
l2 = sideways_length;
function arm_angle() = atan(l2/l1);
function arm_diagonal_length() = sqrt(l1*l1+l2*l2);

// length: center to center length
module arm(length, r=arm_width()/2, center = false, s1=true, s2=true) {
    move = center ? -length/2 : 0;
    translate([move,0,0])
    difference() {
        hull() {
            circle(r=r);
            translate([length,0,0])
            circle(r=r);
        }
        if(s1==true)
        screw_hole();
        if(s2==true)
        translate([length,0,0]) screw_hole();
    }
}
start_angle = 40;
angle = 360;

module sideways(height) {
    grip_h = 9;
    rotate(-90)
    translate([0,arm_length(),0]) {
        translate([0,0,height-grip_h])
        linear_extrude(height=grip_h)
        translate([sideways_length,-arm_width()/2,0]) square([arm_width()/2,20+arm_width()/2]);
    }
}

module arm_left(height) {
    mirror([0,1,0]) {
        gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = angle, start_angle = start_angle);
        rotate(-arm_angle())
        linear_extrude(height=height) {
            arm(length=arm_diagonal_length(), r = arm_width()/2,s2=false);
        }
        sideways(height);
    }
}

module arm_right(height) {
    rotate(180-arm_angle())
    remove_servo_horn(horns=[true,false,true,false], h1=2)
    rotate(180+arm_angle())
    difference() {
        union() {
            rotate([0,0,-360/teeth/2]) // so they're aligned
            gear(r=r, h=height, teeth=teeth, center_r = screw_r()+tolerance(), angle = angle, start_angle = start_angle);
            rotate(-arm_angle())
            linear_extrude(height=height) {
                arm(length=arm_diagonal_length(), r = arm_width()/2,s2=false);
            }
            sideways(height);
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
