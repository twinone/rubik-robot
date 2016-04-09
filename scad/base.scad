use <scad-utils/morphology.scad>
use <gripper_v2/gripper.scad>
use <gripper_v2/buildvars.scad>
use <gripper_v2/gear.scad>
use <gripper_v2/servo.scad>
use <gripper_v2/servo_horn_enclosing.scad>
use <gripper_v2/arm.scad>



function holder_h() = 80; // height to center
function holder_d() = turn_h();
function gripper_r() = turn_r();
function holder_th() = 8;
function holder_w() = gripper_r()*2+holder_th()*2;

function gears_c2c() = 20;
function gear_conversion_factor() = 0.67;
function gear_round() = true;

function base_r() = (gears_c2c()-2)/2;
function servo_gear_f() = 2/(gear_conversion_factor()+1)*gear_conversion_factor();
function turn_gear_f() = 2-servo_gear_f();
function turn_gear_r() = base_r() * turn_gear_f();
function servo_gear_r() = base_r() * servo_gear_f();

function sh_r() = 8/2;
function sh_h() = 3;
function sh_w() = (sh_h()+2)*2;

tgt = 90;
angle = $t < 0.5 ? $t*2*tgt : tgt-($t-0.5)*2*tgt;
angle=-25+180;
module screw_head_padding(h = sh_h()) {
    w = sh_w();
    r = sh_r();
    difference() {
        translate([0,0,h])
        cube([w,w,h*2],center=true);
        
        cylinder(r=r, h=h*2);
    }
}
module screw_holder(h) {
    w = sh_w();
    r = screw_r()+tolerance();
    difference() {
        translate([0,0,h/2])
        cube([w,w,h],center=true);
        
        cylinder(r=r, h=h);
    }
}

module screw(h=4, hh=3) {
    translate([sh_w()/2,sh_w()/2,0]) {
        translate([0,0,hh])
        screw_holder(h=h);
        screw_head_padding(h=hh);
    }
}

module screwy(h,hh) {
    translate([0,0,sh_w()])
    rotate([-90,0,0])
    screw(h=h, hh=hh);
}



module 2screws(h,hh) {
    screwy(h,hh);
    translate([0,0,holder_d()-sh_w()])
    screwy(h,hh);
}

module 4screws(h=sh_h(),hh=3) {
    translate([-sh_w(),0,0])2screws(h,hh);
    translate([holder_w(),0,0]) 2screws(h,hh);
}


module holder_bottom() {
    th = holder_th();
    r = gripper_r() + tolerance()*4;
    w = holder_w();
    h = holder_h();
    bth = th;
    // inner radius
    module shape(r=r, eh) {
        ir = gripper_r()+tolerance();
        linear_extrude(height=eh)
        shell(d=-th)
        difference() {
            polygon([
                [0,0],
                [0,bth],
                [w/2-ir-th,h],
                [w/2+ir+th,h],
                [w,bth],
                [w,0],
            ]);
            translate([w/2,h])
            circle(r=r);
        }
    }
    module base() {
        total_w = holder_d();
        esw = (total_w-5)/2;
        pad=1.5;
        
        m1 = esw+pad/2;
        m2 = esw+5-pad/2;
        shape(r, eh=m1);
        translate([0,0,m1])
        shape(r-1.5,eh=m2-m1);
        translate([0,0,m2])
        shape(r, eh=total_w-m2);
    }
    
    base();   
    
    4screws(hh=3);
    translate([0,holder_h(),0])
    mirror([0,1,0])
    4screws(hh=0);
}

module holder_top() {
    th = holder_th();
    r = gripper_r() + tolerance()*4;
    w = holder_w();
    h = r+th;
    bth = th;
    // inner radius
    module shape(r=r, eh) {
        sqh = sh_h();
        ir = gripper_r()+tolerance();
        linear_extrude(height=eh)
        shell(d=-th)
        
        intersection() {
            union() {
                translate([w/2,w/2])
                circle(r=w/2);
                
                translate([0,h-sqh])
                square([w,sqh]);
            }
            difference() {
                polygon([
                    [0,0],
                    [0,bth],
                    [w/2-ir-th,h],
                    [w/2+ir+th,h],
                    [w,bth],
                    [w,0],
                ]);
                translate([w/2,h])
                circle(r=r);
            }
        }
    }
    module base() {
        total_w = holder_d();
        esw = (total_w-5)/2;
        pad=1.5;
        
        m1 = esw+pad/2;
        m2 = esw+5-pad/2;
        difference() {
            shape(r, eh=m1);
//            translate([w/2,h-gears_c2c(),-1])
//            cylinder(r=enclosing_dst_small()+5,h=servo_base_height()+1);
        }
        
        translate([0,0,m1])
        shape(r-1.5,eh=m2-m1);
        translate([0,0,m2])
        shape(r, eh=total_w-m2);       
    }
    
    sw=3;
    module holder(n) {
        translate([w/2,-gears_c2c()+h,-servo_base_height()*(n+-2)+servo_elevation()-1])
        rotate(0)
        servo_support(w=sw);
    }
    
    cw = servo_d();
    color("purple")
    translate([holder_d(),0,holder_h()+h])
    rotate([-90,0,90]){
        difference() {
            base();
            // vertical
//            translate([w/2-cw/2,h-gears_c2c()-servo_long2(),0])
//            cube([cw,servo_w2()+tolerance()*6,holder_d()+100]);
            // horizontal
            translate([w/2-servo_long2(),h-gears_c2c()-servo_r()-tolerance(),0])
            cube([servo_w2(),servo_d()+tolerance()*2,holder_d()+100]);
        }
        translate([0,h,0])
        mirror([0,1,0])
        4screws(hh=0);
        
        holder(3);
        holder(2);
    }
}

servo_gear_angle = 70;
module servo_gear() {
    rotate([angle+90,0,0])
    rotate([0,90,0]) {
        //enclosing_remove_screws(a1=0,a2=servo_gear_angle,h=50) {
//            linear_extrude(height=4) {
//                rotate([0,0,0])
//                arm(length=enclosing_dst_large(), r=arm_width()/2);
//                rotate([0,0,0+servo_gear_angle])
//                arm(length=enclosing_dst_small(), r=arm_width()/2);
//            }
            rotate(360/30/2)
            remove_servo_horn(horns=[true,false,true,false])
            gear(r=servo_gear_r(), teeth=15*servo_gear_f(), h = 4,teeth_h=1/servo_gear_f(),round=gear_round());
        //}

    }
}

servo_gear_angle = 70;
module turner_gear() {
    //rotate([0,90,0])
    rotate(360/30/2)
    difference() {
        gear(r=turn_gear_r(), teeth=15*turn_gear_f(), h = 4,teeth_h=1/turn_gear_f(),round=gear_round());
        turner_screws(10);
    }
}


module display() {
    color("green")
    translate([0,holder_w()/2,holder_h()])
    mirror()
    rotate([-angle*turn_gear_f(),0,0])
    display_turner();

    translate([holder_d(),holder_w()/2,holder_h()+gears_c2c()]) {
        servo_gear();
        translate([-servo_elevation()-servo_base_height()*1+1,0,0])
        rotate([-90,180,-90]) {
            servo();
//            translate([0,0,servo_elevation()+servo_base_height()])
//            rotate([0,0,90+180+angle])
//            enclosing(a2=servo_gear_angle);
        }
    }

    rotate([90,0,90])
    holder_bottom();
    holder_top();
    
    color("green")
    translate([holder_d(), holder_w()/2, holder_h()])
    rotate([0,90,0])
    turner_gear();
}

module print() {
    $fn=80;
    holder_bottom();
    holder_top();
    
    
    union(){
    !    translate([25,0,0])
        rotate([0,-90,0])
        servo_gear();
        
        turner_gear();
    }

}
display();
//print();
