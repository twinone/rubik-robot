pad = .4;
w = 19.5;
h = 30;
th = 5;
angle=25;

module bed() {
    foot_h = 7;
    bed_l = 80;
    cube([w+th*2,bed_l,th], center=true);
    
    translate([0,bed_l/2-th/2,(th+foot_h)/2])
    rotate([90,0,0])
    cube([w+th*2,foot_h,th], center=true);
}

module phone_holder() {
    rotate([-90,0,0])
    difference() {
        cube([w+th*2,h+th*2,th], center=true);
        translate([0,th,0])
        cube([w,h+th*2,th*2], center=true);
    }
    
    translate([0,0,h/2+th/2+th/2])
    rotate([-angle,0,0])
    bed();
}


phone_holder();