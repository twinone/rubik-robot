use <buildvars.scad>
use <util.scad>
use <arm.scad>
use <servo.scad>
use <gear.scad>
use <servo_horn_enclosing.scad>


function back_x() = 4.31*2;
function back_y() = -gear_r()-10;
function base_rounding_radius() = 7;
function nut_h() = 2.3;

function grip_pad_h() = servo_base_height();

function back_h() = nut_h()+servo_elevation()+2*servo_base_height();
function turn_r() = back_h()/2;
function turn_h() = 30;
function turn_gap_h() = 5;
function turn_gap_d() = 2;
function turn_br() = base_rounding_radius();
function turn_sr() = turn_br()/2;
function turn_mr() = (turn_br()+turn_sr())/2;
function turn_pad() = 2;
function turn_gear_r() = 6;

module dup(vec=[0,1,0]) {
    children();
    mirror(vec) children();
}


module base(servo_hole = true) {
    module main() {
        r = base_rounding_radius();
        module circles(r,a) {
            translate([0,arm_c2c()/2,0])
            circle(r=a?r:r*2);

// // Not needed anymore
//            translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),0])
//            circle(r=r);
                       
            // divide by 6 for 1/3 distance
            translate([back_y(),back_x()/2,0])
            circle(r=r);
        }
        linear_extrude(height=servo_base_height())
        difference() {
            hull() dup() circles(r,false);
            dup() circles(screw_r()+tolerance(),true, $fn=30);
            
        }
    }
    difference() {
        main();
        if (servo_hole) {
            translate([-servo_d()/2-tolerance(),-arm_c2c()/2+servo_w()-servo_r()+tolerance(),0]){
                rotate([0,0,-90]) {
                    cube([servo_w()+2*tolerance(),servo_d()+2*tolerance(),servo_base_height()]);
                }
            }
            translate([0,-arm_c2c()/2,0]){
                rotate([0,0,-90]) {
                    servo_screws();
                }
            }
        }
    }
}

module top() {
    base(false);
}

module show_servo() {
    translate([0,-arm_c2c()/2,0]){
        translate([0,0,-servo_base_height()*1])
        rotate([0,0,-90]) {
            translate([0,0,servo_base_height()]) servo();
        }
//        translate([0,0,servo_elevation()+servo_base_height()])
//        enclosing();
    }
}

module inset_arms(angle = 0) {
    linear_extrude(height=servo_base_height())
    dup() {
        translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),0])
        rotate([0,0,angle])
        arm(length=arm_length());
    }
}

module grippers(angle=0) {
    x = arm_inset_x();
    y = arm_inset_y();
    l = sqrt(x*x + y*y);
    translate([0,0,servo_base_height()])
    linear_extrude(height=servo_elevation()+servo_base_height()*0)
    dup() {
        translate([-arm_length()+cos(angle)*arm_length(),sin(angle)*arm_length(),0])
        translate([arm_length()+y,arm_c2c()/2-x,0]) {
            rotate(-atan(x/y)+180)
            arm(length=l);
            arm(length=l/2);
        }
    }
}


module pusher() {
    linear_extrude(height=servo_elevation())
    arm();
}
module pusher_big() {
    translate([0,0,2])
    linear_extrude(2)
    arm(r=gear_r()/1.2);
    linear_extrude(height=2)
    arm(r=arm_width()/1.5);    
}


/**


OUTPUTS



*/


module turner() {
    
    sr = turn_sr();
    br = turn_br();
    mr = turn_mr();
    h = back_h();
    
    turner_height = 10;
    module s(h) {
        linear_extrude(height=h)
        difference() {
            hull()
            dup()
            translate([-(br+sr)/2+sr,back_x()/2,0])
            circle(r=mr);

            dup()
            translate([0, back_x()/2,0])
            circle(r=screw_r()); // without tolerance for better grip
        }
    }
    translate([0,0,servo_base_height()])
    s(h-servo_base_height()*2);
    
    gap_h = turn_gap_h();
    gap_d = turn_gap_d();
    pad = turn_pad();
    sd = 10;
    difference() {
        translate([-br+sr-pad,0,h/2])
        rotate([0,-90,0]) {
            difference() {
                difference() {
                    cylinder(r=h/2, h=turn_h()+sr);
                    translate([0,0,sr+(turn_h())/2-gap_h/2])
                    difference() {
                        cylinder(h=gap_h, r=h/2+1);
                        cylinder(h=gap_h, r=h/2-gap_d);
                    }
                }
                
                translate([0,0,turn_h()+sr-sd])
                turner_screws(h=sd);
    //            cylinder(r=screw_r(),h=sd); // yes, without tolerance
            }
    //        translate([0,0,turn_h()+sr])
    //        gear(r=turn_gear_r(), teeth=15, h = 4);
        }
        s(h);
    }
}

module turner_screws(h, r=screw_r()) {
    sdst = 4;
    n = 3;
    for (i = [0:n-1])
    rotate(360/n*i)
    translate([sdst,0,0])
    cylinder(r=r,h=h);
}

module display_turner() {
    translate([turn_br()+turn_pad(),0,-turn_r()]) turner();
}



module display_gripper() {
    tgt = 90;
    arm_angle = $t < 0.5 ? $t*2*tgt : tgt-($t-0.5)*2*tgt;
    
    // BASE
    //color("red")
    %translate([0,0,servo_base_height()]) {
        base();
        show_servo();
    }
    
    %translate([0,0,servo_elevation()+servo_base_height()*2+nut_h()])
    top();


    // GEAR ARMS
    translate([0,0,servo_elevation()*0+servo_base_height()*3])
    arms(height = servo_base_height(), center = true, angle=arm_angle);
  

    // PUSHERS
    translate([0,0,servo_base_height()])
    color("orange") {
        translate([0,arm_c2c()/2,servo_base_height()*1])
        pusher_big();
    }
    
    color("green")
    translate([back_y(),0,servo_base_height()])
    turner();
}


// to print:
module print() {
    arm_angle = 0;

    $fn=80;
    
    translate([-30,50,servo_base_height()])
    base();
    
    translate([-30,120,servo_base_height()])
    top();
    
    
    translate([0,-50,0])
    arms(height = servo_base_height(), center = true, extrasep = 5);
    
    translate([30,0,0])
    !rotate([180,0,0])
    pusher_big();
    
    turner();
    
    //enclosing(shhb=2);

}

//translate([back_y(),0,servo_base_height()]*-1)
//translate([turn_br()+turn_pad(),0,-turn_r()])
//display_gripper();
print();
