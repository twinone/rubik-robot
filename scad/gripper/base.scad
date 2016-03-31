use <buildvars.scad>
use <util.scad>
use <arm.scad>
use <servo.scad>
use <servo_horn_enclosing.scad>

module dup(vec=[0,1,0]) {
    children();
    mirror(vec) children();
}


module base(servo_hole = true) {
    module main() {
        r = 7;
        module circles(r,a) {
            translate([0,arm_c2c()/2,0])
            circle(r=a?r:r*2);

            translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),0])
            circle(r=r);
            
            translate([-gear_r()-10,arm_c2c()/2,0])
            circle(r=r);
           
            // divide by 6 for 1/3 distance
            translate([-gear_r()-10,arm_c2c()/6,0])
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
        translate([0,0,servo_elevation()+servo_base_height()])
        enclosing();
    }
}

function arm_inset_x() = arm_width() + 2;
function arm_inset_y() = gear_r()+arm_width()*2;

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
    linear_extrude(height=servo_elevation()+servo_base_height())
    arm();
}


/**


OUTPUTS



*/


module display_gripper() {
    arm_angle = $t < 0.5 ? $t*2*90 : 90-($t-0.5)*2*90;

    // BASE
    color("red")
    base();
    show_servo();
    
    translate([0,0,servo_elevation()+servo_base_height()*3])
    %top();


    // GEAR ARMS
    translate([0,0,servo_elevation()+servo_base_height()*2])
    arms(height = servo_base_height(), center = true, angle=arm_angle);



    // GRIPPERS
    color("DarkMagenta")
    translate([0,0,servo_base_height()]*1)
    grippers(angle=arm_angle);


    // INSET ARMS
    color("green") {
        translate([0,0,servo_base_height()*1])
        inset_arms(angle=arm_angle);
        translate([0,0,servo_elevation() + servo_base_height()*2])
        inset_arms(angle=arm_angle);
    }



    // PUSHERS
    color("orange") {
        translate([0,arm_c2c()/2,servo_base_height()*1])
        pusher_big();
        
        dup()
        translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),servo_base_height()*2])
        pusher();
    }
}

// to print:
module print() {
    arm_angle = 0;

    $fn=80;
    
    translate([-50,30,servo_base_height()])
    base();
    
    translate([-50,130,servo_base_height()])
    top();
    
    
    
    translate([0,-50,0])
    arms(height = servo_base_height(), center = true);
    
    translate([-10,80,0])
    grippers();
    
    
    translate([0,0,0]) {
        inset_arms();
        translate([0,15,0]) inset_arms();     
    }
    translate([0,0,0]) {
        pusher();
        translate([10,0,0]) pusher();
        translate([20,0,0]) pusher_big();
    }
}


display_gripper();

//translate([-150,0,0])
//print();
