base_s = 130;
thickness = 3;

screw_r = 3/2;
screw_spacing = 10;
screw_num = 12;

$fn=20;
module screw() {
    #cylinder(r=screw_r, h=20,center=true);
}

margin2 = base_s-(screw_num -1)* (screw_spacing);
margin = margin2 / 2;

module base() {
    translate([-base_s/2,-base_s/2,0])
    difference() {
        translate([0,0,thickness/2])
        cube([base_s,base_s,thickness]);
        
        for (i = [0:screw_num-1]) {
            for (j = [0:screw_num-1]) {
                translate([margin+i*screw_spacing,margin+j*screw_spacing,0]) screw();
            } 
        }
    }
}
difference() {
    base();
    l = 25;
    s = 30;
    for (i = [1,-1]) for (j = [1,-1]) {
        translate([-l*i,-l*j,0])
        #cube([s,s,30],center=true);
    }
}