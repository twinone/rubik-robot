use <buildvars.scad>
use <gripper/gear.scad>
use <gripper/screw.scad>

cube_spacing = 3;

module rotating_ring() {
    
    w = cube_width();
    r = sqrt(w*w*2)/2 * 1.5;
    h = 10;
    
    module bearing() {
        bh = h/4;
        difference() {
            cylinder(r=r*0.9,h=bh);
            cylinder(r=r*0.9-3,h=bh);
        }
    }
    
    
    difference() {
        gear(r=r, teeth=20, h=h);
        
        cube([w+cube_spacing,w+cube_spacing,h*3], center=true);
        bearing();
    }    
}

module rotating_base() {
    w = cube_width();
    h = cube_width() / 4;
    t = cube_holder_padding();
    th = 4 +t;
    outer_cube_w = w + th*2;
    translate(outer_cube_w/2*[-1,-1,0])
    difference() {
        cube([outer_cube_w,outer_cube_w,h+th]);
        translate([0-t,th-t,th-t])
        cube([w+th+t*2,w+t*2,h+t*2]);
    }
    
}

rotating_base();