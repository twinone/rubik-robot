
function big_cr() = 13/2; // center of servo radius
function big_r1() = 6; // radius of (horn center)
function big_r2() = 5.6/2; // radius of horn (outer)
function dst() = 36.5/2-big_r2();
function dst_6() = 34/2-big_r2();
function horn_h() = 2.8;

module big_horn() {
    linear_extrude(height = horn_h())
    hull() {
        circle(r=big_r1()+.0);
        translate([dst_6(),0,0])
        circle(r=big_r2()+.0, $fn=50);
    }
}

// h; height of piece you want to cut out of
module big_remove_enclosing(h) {
    difference() {
        children();
        
        for (i = [0:5]) {
            translate([0,0,h-horn_h()])
            rotate([0,0,i*60])
            big_horn();
        }
        cylinder(h=h, r=big_cr());
    }
}

