use <buildvars.scad>
use <util.scad>

// global
h1 = 2.5;
horn_h = 1.5;
horn_rl = 7/2; // horn large radius
th = 1.2;

// per arm
horn_c2c = 15.4; // center to center
horn_rs = 3.5/2; // horn small radius


screw_head_r = 5/2;
min_screw_dst = screw_head_r + 10;

total_h = h1+horn_h;

// screw
screw_r = 3/2;
screw_c2c = horn_c2c+th+horn_rl*2+horn_rs*2+screw_r*2;
screw_th = 3; // thickness of the screw holder wings


screw_tolerance = tolerance();
t = tolerance();
t2 = screw_tolerance;
$fn=30;
module 2d_horn(rs, rl, c2c) {
    hull() {
        circle(r=rl);
        translate([c2c,0,0]) {
            circle(r=rs);
        }
    }
}

module 2d_outer_horn() {
        2d_horn(horn_rs+th+t, horn_rl+th+t, horn_c2c);
}
module outer_horn() {
    linear_extrude(height=total_h)
    2d_outer_horn();
}

module screw_holder(dst, h) {
    linear_extrude(height=h)
    difference() {
        hull() {
            translate([dst,0,0])
            circle(r=screw_r+th+t);
            circle(r=horn_rl+th+t);
        }
        circle(r=horn_rl+th+t);
        translate([dst,0,0])
        screw_hole();
    }
}



module remove_servo_horn(h=10, horns=[true,false,false,false]) {
    
    module horn_big() {
        translate([0,0,h1])
        linear_extrude(height=h)
        2d_horn(horn_rs+t, horn_rl+t, horn_c2c);
    }
    difference() {
        children();
        // center
        if (horns[0] == true)
        linear_extrude(height=h1) circle(r=horn_rl+t);

        if (horns[2] == true)
        horn_big();
        rotate(180) horn_big();        
    }
}


module main_enclosing() {
    linear_extrude(height=h1)
    2d_outer_horn();

    translate([0,0,h1])
    linear_extrude(height=horn_h)
    2d_outer_horn();
}


function enclosing_dst_small() = max(screw_r+th+t + horn_rl+th+t, min_screw_dst);
function enclosing_dst_large() = screw_r+th+t + horn_c2c+horn_rs+th+t;

module screws(a1, a2) {
    //translate([-(screw_r+th+t + horn_rl+th+t),0,0])
    rotate(a2)
    screw_holder(enclosing_dst_small(), total_h);

    difference() {
        rotate(a1)
        screw_holder(enclosing_dst_large(), total_h);
        outer_horn();
    }
}

// shht = screw head height top
// shhb = screw head height bottom
// shr =  screw head radius
module enclosing(shht=0, shhb=0, shr=3.5, a1=0, a2=180) {
    remove_servo_horn()
    difference() {
        union() {
            main_enclosing();
            screws(a1,a2);
        }
        rotate(a1)
        translate([enclosing_dst_large(),0,0])
        cylinder(h=shhb,r=shr);
        rotate(a2)
        translate([enclosing_dst_small(),0,0])
        cylinder(h=shhb,r=shr);

        rotate(a1)
        translate([enclosing_dst_large(),0,total_h-shht])
        cylinder(h=shht,r=shr);
        rotate(a2)
        translate([enclosing_dst_small(),0,total_h-shht])
        cylinder(h=shht,r=shr);
    }
}
//enclosing();

module enclosing_remove_screws(a1 = 0, a2 = 180, h=10) {
    difference() { 
        children();
        linear_extrude(h=h)
        enclosing_centered_holes(a1=a1,a2=a2);
    }
}


module enclosing_centered_holes(a1 = 0, a2 = 180) {
    screw_hole();
    rotate(a2)
    translate([enclosing_dst_small(),0,0]) screw_hole();
    rotate(a1)
    translate([enclosing_dst_large(),0,0]) screw_hole();
}