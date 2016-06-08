function cube_size() = 56;
function tolerance() = 0.2;
function screw_r() = 3/2;
function arm_width() = 7;
function arm_length() = 35;
function servo_base_height() = 4;
function servo_elevation() = 5;
function gear_r() = 11;
function arm_c2c() = gear_r()*2.35;

function arm_inset_x() = arm_width() + 2;
function arm_inset_y() = gear_r()+arm_width();

function back_x() = 4.31*2;
function back_y() = -gear_r()-10;
function base_rounding_radius() = 7;
function nut_h() = 2.3;

function grip_pad_h() = servo_base_height();

function back_h() = nut_h()+servo_elevation()+2*servo_base_height();
function gripper_r() = back_h()/2;
function turn_h() = 30;
function turn_gap_h() = 5;
function turn_gap_d() = 2;
function turn_br() = base_rounding_radius();
function turn_sr() = turn_br()/2;
function turn_mr() = (turn_br()+turn_sr())/2;
function turn_pad() = 2;
function turn_gear_r() = 6;



function sh_r() = 8/2;
function sh_h() = 3;
function sh_w() = 10;


function holder_h() = 80; // height to center
function holder_d() = turn_h();
function holder_th() = 8;
function holder_w() = gripper_r()*2+holder_th()*2;
function holder_dst() = 183;


function gears_c2c() = 36;
function gear_conversion_factor() = 1; // old = 0.67;
function gear_round() = true;

function base_r() = (gears_c2c()-3)/2;
function servo_gear_f() = 2/(gear_conversion_factor()+1)*gear_conversion_factor();
function turn_gear_f() = 2-servo_gear_f();
function turn_gear_r() = base_r() * turn_gear_f();
function servo_gear_r() = base_r() * servo_gear_f();

function rail_sep_w() = sh_w()/4;

