use <buildvars.scad>
use <gripper.scad>
use <util/screw.scad>
use <util/servo.scad>
use <scad-utils/morphology.scad>


module holder_bottom() {
    th = holder_th();
    r = gripper_r() + tolerance()*4;
    w = holder_w();
    h = holder_h();
    bth = th;
    // inner radius
    module shape(r=r, eh) {
        ir = gripper_r()+tolerance();
        linear_extrude(height=eh)
        shell(d=-th)
        difference() {
            polygon([
                [0,0],
                [0,bth],
                [w/2-ir-th,h],
                [w/2+ir+th,h],
                [w,bth],
                [w,0],
            ]);
            translate([w/2,h])
            circle(r=r);
        }
    }
    module base() {
        total_w = holder_d();
        esw = (total_w-5)/2;
        pad=1.5;
        
        m1 = esw+pad/2;
        m2 = esw+5-pad/2;
        shape(r, eh=total_w);
    }
    
    base();   
    
    4screws(hh=3);
    translate([0,holder_h(),0])
    mirror([0,1,0])
    4screws(hh=0);
}


module holder_top() {
        th = holder_th();
        r = gripper_r() + tolerance()*2;
    w = holder_w();
    h = r+th;
    bth = th;
    // inner radius
    module shape(r=r, eh) {
        sqh = sh_h();
        ir = gripper_r()+tolerance();
        linear_extrude(height=eh)
        shell(d=-th)
        
        intersection() {
            union() {
                translate([w/2,w/2])
                circle(r=w/2);
                
                translate([0,h-sqh])
                square([w,sqh]);
            }
            difference() {
                polygon([
                    [0,0],
                    [0,bth],
                    [w/2-ir-th,h],
                    [w/2+ir+th,h],
                    [w,bth],
                    [w,0],
                ]);
                translate([w/2,h])
                circle(r=r);
            }
        }
    }
    
    sw=3;
    module holder() {
        translate([w/2,-gears_c2c()+h,-servo_base_height()+servo_elevation()-1])
        rotate(0)
        servo_adapter(th1=5, th2=2, eh=8);
    }
    
    translate([holder_d(),0,holder_h()+h])
    rotate([-90,0,90]){
        difference() {
            union(){
                holder();
                shape(r, eh=holder_d());
            }
            translate([holder_w()/2,h])
            cylinder(r=r, h=holder_d());
        }
        translate([0,h,0])
        mirror([0,1,0])
        4screws(hh=0);
        
    }
}


holder_top();
holder_bottom();
