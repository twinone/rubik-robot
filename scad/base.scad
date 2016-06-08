use <scad-utils/morphology.scad>
use <gripper/gripper.scad>
use <gripper/buildvars.scad>
use <gripper/gear.scad>
use <gripper/big_servo_enclosing.scad>
use <gripper/micro_servo_horn_enclosing.scad>
use <gripper/arm.scad>
use <gripper/micro_servo.scad>
    
function holder_h() = 80; // height to center
function holder_d() = turn_h();
function gripper_r() = turn_r();
function holder_th() = 8;
function holder_w() = gripper_r()*2+holder_th()*2;
function holder_dst() = 183;

function gears_c2c() = 36;
function gear_conversion_factor() = 1; // old = 0.67;
function gear_round() = true;

function base_r() = (gears_c2c()-3)/2;
function servo_gear_f() = 2/(gear_conversion_factor()+1)*gear_conversion_factor();
function turn_gear_f() = 2-servo_gear_f();
function turn_gear_r() = base_r() * turn_gear_f();
function servo_gear_r() = base_r() * servo_gear_f();

function sh_r() = 8/2;
function sh_h() = 3;
function sh_w() = 10;

function rail_sep_w() = sh_w()/4;


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


module servo_adapter(th1, th2, eh=3, extra=15) {
    pad = .4;
    r = 19.9 / 2;
    d = r*2 + pad;
    w1 = 40.4 + pad;
    w2 = 54 + pad;
    sw = (w2-w1)/2;
    h2 = 49; // height with horn inserted
    screw_r = .7;
        screw_d = 4;
        
        translate([-d/2-th1,-w2/2-th2-w1/2+r,0])
        difference() {
            cube([d,w2+extra,eh]+[th1,th2,0]*2);
            translate([0,sw,0]+[th1,th2,0])
            cube([d,w1,eh]);
            
            translate([th1,th2+sw/2,0]+[screw_d,0,0]) cylinder(r=screw_r, h=eh);
            translate([th1,th2+sw/2,0]+[d-screw_d,0,0]) cylinder(r=screw_r, h=eh);
            translate([0,w1+sw,0]) {
                translate([th1,th2+sw/2,0]+[screw_d,0,0]) cylinder(r=screw_r, h=eh);
                translate([th1,th2+sw/2,0]+[d-screw_d,0,0]) cylinder(r=screw_r, h=eh);
            }
        }
        
    }


    module holder_top() {
        th = holder_th();
        r = gripper_r() + tolerance()*2;
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
    
    sw=3;
    module holder() {
        translate([w/2,-gears_c2c()+h,-servo_base_height()+servo_elevation()-1])
        rotate(0)
        //servo_support(w=sw);
        servo_adapter(th1=5, th2=2, eh=8);
    }
    
    cw = servo_d();
    //color("purple")
    translate([holder_d(),0,holder_h()+h])
    rotate([-90,0,90]){
        difference() {
            union(){        holder();

            shape(r, eh=holder_d());
            }
            translate([holder_w()/2,h])
            cylinder(r=r, h=holder_d());
        }
        translate([0,h,0])
        mirror([0,1,0])
        4screws(hh=0);
        
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
            big_remove_enclosing(h=gear_h)
            gear(r=servo_gear_r(), teeth=15*servo_gear_f(), h = gear_h,teeth_h=1/servo_gear_f(),round=gear_round());
        //}

    }
}

gear_h = 7;
servo_gear_angle = 70;
module turner_gear() {
    //rotate([0,90,0])
    rotate(360/30/2)
    difference() {
        gear(r=turn_gear_r(), teeth=15*turn_gear_f(), h = gear_h,teeth_h=1/turn_gear_f(),round=gear_round());
        turner_screws(gear_h*2, $fn=20);
    }
}



module foot() {
    dst = holder_dst();
    w = holder_w()/2;
    r = dst/2-w;
    module c(r) {
        intersection() {
            translate([w,w])
            square([dst,dst]);
            translate([w,w])
            circle(r=r,h=2);
        }
    }

    linear_extrude(height=sh_h()*2) {
        intersection() {
            union() {
                difference() {
                    c(r+sh_w());
                    c(r);
                }
                difference() {
                    c(r+sh_w()+holder_d()-sh_w());
                    c(r+holder_d()-sh_w());
                }
            }
            translate([1,1]*(holder_w()/2+sh_w()))
            square([dst,dst]);

        }
        
    }
    
    module m(x,y) {
        translate([x,y,0]) children();
        translate([y,x,0]) children();
    }
    difference() {
        translate([0,0,sh_h()*2])
        linear_extrude(height=sh_h()*2) {
            difference() {
                c(r+sh_w());
                c(r);
            }
            difference() {
                c(r+sh_w()+holder_d()-sh_w());
                c(r+holder_d()-sh_w());
            }
        }
        translate([1,1,0]*(holder_w()/2+sh_w()*2))
        cube([dst,dst,30]);
        
        m(dst/2+sh_w()/2,holder_w()/2+sh_w()/2) cylinder(h=100,r=screw_r()+tolerance());
        m(dst/2+holder_d()-sh_w()/2,holder_w()/2+sh_w()/2) cylinder(h=100,r=screw_r()+tolerance());
    }
    
    a = 40;
    n = 8;
    cw = rail_sep_w();
    translate([w,w,0])
    for(i = [0:n-1])
    rotate(a/2+(90-a)/(n-1)*i)
    translate([dst/2-w+sh_w()/2,-cw/2,0])
    cube([holder_d()-sh_w(),cw,sh_h()*2]);

       
}

module cable_holder() {
    inner_w = rail_sep_w()-tolerance();
    inner_r = inner_w/2;
    th = 2;
    w=inner_w+2*th;
    r=w/2;
    h = sh_h()*6;
    d = (holder_w()-sh_w()*2-tolerance())/3*2;
    linear_extrude(height=d)
    difference() {
        hull() {
            translate([h,0,0])circle(r=r);
            translate([0,-w/2,0]) square([w,w]);
        }
        hull() {
            translate([h,0,0]) circle(r=inner_r);
            translate([0,-inner_w/2,0]) square([inner_w,inner_w]);
        }
    }
}




module display() {
    translate([0,holder_w()/2,holder_h()])
    mirror()
    
    // full gripper
    rotate([90,0,0])
    translate([back_y(),0,servo_base_height()]*-1)
    translate([turn_br()+turn_pad(),0,-turn_r()])
    display_gripper();

    // only turner
    // rotate([-angle*turn_gear_f(),0,0])
    // display_turner();

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


module display4() {
    for (i = [0:3]) {
        rotate(i*90) {
            translate([holder_dst()/2,-holder_w()/2,0])
            display();
            foot();
            translate([0,0,holder_h()-sh_h()])
            %foot();
        }
    }
    
    %translate([0,0,holder_h()])
    cube([1,1,1]*56,center=true);
}

module print() {
    $fn=80;
    holder_bottom();
    holder_top();
    
    union(){
        translate([25,0,0])
        rotate([0,-90,0])
        servo_gear();
        
        turner_gear();
    }
    foot($fn=150);
    
    cable_holder();


}

//turner_gear();

//holder_top($fn=50);
display4();
// print();
