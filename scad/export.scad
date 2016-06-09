use <buildvars.scad>
use <arm.scad>
use <gripper.scad>
use <holder.scad>
use <gears.scad>
use <foot.scad>

all = false;
part = "";

// ARMS
module export_arm(left = true, right = true) {
    translate([0,0,arm_height()]) rotate([0,180,0]) arms(right=right, left=left);
}
module export_arm_left() { export_arm(right=false); }
module export_arm_right() { export_arm(left=false); }

module export_grip_top() { top(); }
module export_grip_bottom() { base(); }
module export_grip_pad() { pusher_big(); }
module export_grip_cylinder() { translate([0,0,turn_h()]) rotate([0,-90,0]) display_turner(); }


module export_holder_top() { translate([0,0,holder_d()]) rotate([0,90,0]) translate([0,0,-holder_h()]) holder_top(); }
module export_holder_bottom() { holder_bottom(); }
    
module export_servo_gear() { rotate([0,-90,0]) servo_gear(); }
module export_grip_gear() { turner_gear(); }
module export_foot() { foot(); }


module export() {
    if (part == "arm_left" || all) export_arm_left();
    if (part == "arm_right" || all) export_arm_right();
    if (part == "grip_top" || all) export_grip_top();
    if (part == "grip_bottom" || all) export_grip_bottom();
    if (part == "grip_pad" || all) export_grip_pad();
    if (part == "grip_cylinder" || all) export_grip_cylinder();
    if (part == "holder_top" || all) export_holder_top();
    if (part == "holder_bottom" || all) export_holder_bottom();
    if (part == "servo_gear" || all) export_servo_gear();
    if (part == "grip_gear" || all) export_grip_gear();
    if (part == "foot" || all) export_foot();
}

export();