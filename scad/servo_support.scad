$fn=100;

// Thickness
th_b = 3; //bottom
th_s = 5; // sides
th_d = 3; // depth
box_w = 22;
box_d = 12;
box_h = 15.5;
// Screw radius
sr = 1.1;
sh = 5;
// Screw center to screw center
sc2sc = 27.5;
sc2c = 8.5; // (closest) screw to center
margin = 0.6;

module servo_support() {
	difference() {
		// Outer
		cube([box_w+th_s*2,box_d+th_d*2,box_h+th_b]);
		// Inner
		translate([th_s-margin,th_d-margin,th_b-margin])
		cube([box_w+2*margin,box_d+2*margin,box_h+10]);
		translate([th_s-margin,th_d-margin,th_b*2-margin])
		cube([box_w+2*margin,box_d+10,box_h+10]);

		// Cables...
		translate([box_w+th_s-1,box_d/2+th_d,4.5+th_b])
		rotate([0,90,0])
		cylinder(r=2.5,h=th_s+2);

		translate([box_w+th_s-1,box_d/2+th_d,3.5+th_b])
		rotate([45,0,0])
		cube([th_s+2,box_d*2,2]);

		// Screws
		translate([th_s/2,box_d/2+th_d,box_h+th_b-sh])
		cylinder(r=sr-0.1,h=sh);
		
		translate([th_s+box_w,0,0])
		translate([th_s/2,box_d/2+th_d,box_h+th_b-sh])
		cylinder(r=sr-0.1,h=sh);


	}
}

servo_support();