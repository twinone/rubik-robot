use <scad-utils/morphology.scad>

use <buildvars.scad>
use <gripper.scad>
use <arm.scad>
use <holder.scad>
use <foot.scad>
use <gears.scad>

use <util/gear.scad>
use <util/servo_enclosing.scad>
use <util/mservo_enclosing.scad>
use <util/mservo.scad>
    

tgt = 90;
angle = $t < 0.5 ? $t*2*tgt : tgt-($t-0.5)*2*tgt;
angle=-25+180;

module display() {
    translate([0,holder_w()/2,holder_h()])
    mirror()
    
    rotate([90,0,0])
    translate([back_y(),0,servo_base_height()]*-1)
    translate([turn_br()+turn_pad(),0,-gripper_r()])
    display_gripper();

    translate([holder_d(),holder_w()/2,holder_h()+gears_c2c()]) {
        servo_gear();
        translate([-servo_elevation()-servo_base_height()*1+1,0,0])
        rotate([-90,180,-90]) {
            servo();
        }
    }

    rotate([90,0,90])
    holder_bottom();
    holder_top();
    
    color("green")
    translate([holder_d(), holder_w()/2, holder_h()])
    rotate([0,90,0])
    turner_gear();
}


module display4() {
    for (i = [0:3]) {
        rotate(i*90) {
            translate([holder_dst()/2,-holder_w()/2,0])
            display();
            foot();
            translate([0,0,holder_h()-sh_h()])
            %foot();
        }
    }
    
    %translate([0,0,holder_h()])
    cube([1,1,1]*56,center=true);
}


display4();
