use <buildvars.scad>

module foot() {
    dst = holder_dst();
    w = holder_w()/2;
    r = dst/2-w;
    module c(r) {
        intersection() {
            translate([w,w])
            square([dst,dst]);
            translate([w,w])
            circle(r=r,h=2);
        }
    }

    linear_extrude(height=sh_h()*2) {
        intersection() {
            union() {
                difference() {
                    c(r+sh_w());
                    c(r);
                }
                difference() {
                    c(r+sh_w()+holder_d()-sh_w());
                    c(r+holder_d()-sh_w());
                }
            }
            translate([1,1]*(holder_w()/2+sh_w()))
            square([dst,dst]);

        }
        
    }
    
    module m(x,y) {
        translate([x,y,0]) children();
        translate([y,x,0]) children();
    }
    difference() {
        translate([0,0,sh_h()*2])
        linear_extrude(height=sh_h()*2) {
            difference() {
                c(r+sh_w());
                c(r);
            }
            difference() {
                c(r+sh_w()+holder_d()-sh_w());
                c(r+holder_d()-sh_w());
            }
        }
        translate([1,1,0]*(holder_w()/2+sh_w()*2))
        cube([dst,dst,30]);
        
        m(dst/2+sh_w()/2,holder_w()/2+sh_w()/2) cylinder(h=100,r=screw_r()+tolerance());
        m(dst/2+holder_d()-sh_w()/2,holder_w()/2+sh_w()/2) cylinder(h=100,r=screw_r()+tolerance());
    }
    
    a = 40;
    n = 8;
    cw = rail_sep_w();
    translate([w,w,0])
    for(i = [0:n-1])
    rotate(a/2+(90-a)/(n-1)*i)
    translate([dst/2-w+sh_w()/2,-cw/2,0])
    cube([holder_d()-sh_w(),cw,sh_h()*2]);

       
}

foot();