use <buildvars.scad>
use <util.scad>
use <arm.scad>
use <servo.scad>
use <servo_horn_enclosing.scad>

module dup(vec=[0,1,0]) {
    children();
    mirror(vec) children();
}

module base() {
    module main() {
        r = 7;
        module circles(r,a) {
            translate([0,arm_c2c()/2,0])
            circle(r=a?r:r*2);

            translate([-gear_r()-10,arm_c2c()/2,0])
            circle(r=r);
            translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),0])
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
        translate([-servo_d()/2-tolerance(),-arm_c2c()/2+servo_w()-servo_r()+tolerance(),0]){
            rotate([0,0,-90]) {
                cube([servo_w()+2*tolerance(),servo_d()+2*tolerance(),servo_base_height()]);
            }
        }
    }

    translate([0,-arm_c2c()/2,-servo_base_height()]){
        rotate([0,0,-90]) {
            servo_support();
        }
    }

}

module show_servo() {
    translate([0,-arm_c2c()/2,0]){
        translate([0,0,-servo_base_height()*2])
        rotate([0,0,-90]) {
            translate([0,0,servo_base_height()]) servo();
        }
        translate([0,0,servo_elevation()*1])
        enclosing();
    }
}

function arm_inset_x() = arm_width() + 2;
function arm_inset_y() = gear_r()+arm_width()*2;

module inset_arms() {
    linear_extrude(height=servo_base_height())
    dup() {
        translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),0])
        arm(length=arm_length());
    }
}

module grippers() {
    x = arm_inset_x();
    y = arm_inset_y();
    l = sqrt(x*x + y*y);
    linear_extrude(height=servo_elevation()+servo_base_height()*1)
    dup() {
        translate([arm_length()+y,arm_c2c()/2-x,0]) {
            rotate(-atan(x/y)+180)
            arm(length=l);
            arm(length=l);
        }
    }
}


module pusher() {
    linear_extrude(height=servo_elevation())
    arm();
}


/**


OUTPUTS



*/


module display() {
    // BASE
    color("red")
    base();
    //show_servo();


    // GEAR ARMS
    translate([0,0,servo_elevation()+servo_base_height()*1])
    arms(height = servo_base_height(), center = true);



    // GRIPPERS
    color("DarkMagenta")
    translate([0,0,servo_base_height()]*0)
    grippers();


    // INSET ARMS
    color("green") {
        translate([0,0,servo_base_height()*-1])
        inset_arms();
        translate([0,0,servo_elevation() + servo_base_height()*1])
        inset_arms();
    }



    // PUSHERS
    color("orange") {
        translate([0,arm_c2c()/2,servo_base_height()])
        pusher();
        
        dup()
        translate([arm_inset_y(),arm_c2c()/2-arm_inset_x(),servo_base_height()])
        pusher();
    }
}

// to print:
module print() {
    
    translate([-50,30,servo_base_height()])
    rotate([0,180,0])
    base();
    
    translate([0,-50,0])
    arms(height = servo_base_height(), center = true);
    
    translate([-50,60,0])
    grippers();
    
    
    inset_arms();
    translate([0,10,0]) inset_arms();
    
    pusher();
    translate([10,0,0]) pusher();
    translate([20,0,0]) pusher();
}

display();

translate([-150,0,0])
print();
