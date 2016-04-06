module 2d_gear(r, teeth, angle, start_angle) {
    circle(r=r);
    for (i = [start_angle:360/teeth:angle+start_angle])
    rotate([0,0,i])
    translate([r,0,0]) {
        children();
    }
}

module gear(r, h, teeth, center_r = 0, angle = 360, start_angle = 0, teeth_h = 1) {
    linear_extrude(height=h)
    difference() {
        2d_gear(r=r, teeth=teeth, angle=angle, start_angle=start_angle) {
            scale([2,1,1] * r*1.5 / 10*teeth_h)
            translate([-0.1,1.5,0])
            rotate([0,0,-90])
            polygon([
                [0.75,0],
                [1,1],
                [2,1],
                [2.25,0],
            ]);
            
        }
        circle(r=center_r, $fn=30);
    }
}

 gear(r=10, h=3, teeth=15, center_r = 3/2+0.2, angle = 180-30*2, start_angle = 30);
