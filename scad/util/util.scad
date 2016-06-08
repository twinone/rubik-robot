use <../buildvars.scad>

module screw_hole() {
	circle(r=screw_r()+tolerance());
}

