use <../buildvars.scad>
use <util.scad>

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

screw_holder(3);
