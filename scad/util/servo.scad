use <../buildvars.scad>

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
