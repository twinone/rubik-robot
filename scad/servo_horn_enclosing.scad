// Public variables

// global
h1 = 2.5;
horn_h = 1.5;
horn_rl = 7/2; // horn large radius
tolerance = 0.3;
th = 1.2;

// per arm
horn_c2c = 15.4; // center to center
horn_rs = 3.5/2; // horn small radius


screw_head_th = 2;
screw_head_r = 5/2;
min_screw_dst = screw_head_r + 10;



// screw
screw_r = 3/2;
screw_c2c = horn_c2c+th+horn_rl*2+horn_rs*2+screw_r*2;
screw_tolerance = 0.2;
screw_th = 3; // thickness of the screw holder wings



t = tolerance;
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
    linear_extrude(height=h1+horn_h)
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
        circle(r=screw_r+t);
    }
}


module main_enclosing() {
    linear_extrude(height=h1)
        difference() {
        2d_outer_horn();
        circle(r=horn_rl+t);
    }

    translate([0,0,h1])
    linear_extrude(height=horn_h)
    difference() {
        2d_outer_horn();
        2d_horn(horn_rs+t, horn_rl+t, horn_c2c);
    }
}

module screws() {
    //translate([-(screw_r+th+t + horn_rl+th+t),0,0])
    rotate([0,0,180])
    screw_holder(max(screw_r+th+t + horn_rl+th+t, min_screw_dst), h1+horn_h);

    difference() {
        screw_holder(screw_r+th+t + horn_c2c+horn_rs+th+t, h1+horn_h);
        outer_horn();
    }
}


main_enclosing();
screws();