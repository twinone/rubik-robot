use <buildvars.scad>

function servo_w() = 22.5;
function servo_d() = 12;
function servo_h() = 22;
function servo_w2() = 32;
function servo_r() = servo_d()/2;
function servo_flap() = (servo_w2() -servo_w()) /2;
function servo_short() = servo_r();
function servo_long() = servo_w()-servo_short();
function servo_short2() = servo_short()+servo_flap();
function servo_long2() = servo_long()+servo_flap();

module servo_support(w = 0, ext=0) {
    h = 4;
    d = servo_w2() - servo_w();
    translate([-servo_w()+servo_r()-d/2,-servo_d()/2,0])
    //cube([servo_w(),servo_d(),servo_h()]);
    difference() {
        translate([0,-w-tolerance(),0])
        cube([servo_w2()+ext,servo_d()+w*2+tolerance()*2,h]);
        translate([(servo_w2()-servo_w())/2,0,0])
        translate([-tolerance(),-tolerance(),0])
        cube([servo_w()+tolerance()*2,servo_d()+tolerance()*2,h]);
        translate([2,servo_d()/2,0]) cylinder(h=9,r=1,center=true);
        translate([servo_w2()-2,servo_d()/2,0]) cylinder(h=9,r=1,center=true);
    }
}

module servo_screws() {
    h = 4;
    d = servo_w2() - servo_w();
    color("red")
    translate([-servo_w()+servo_r()-d/2,-servo_d()/2,0])  {
        translate([2,servo_d()/2,0]) cylinder(h=4,r=1);
        translate([servo_w2()-2,servo_d()/2,0]) cylinder(h=4,r=1);
    }
}
servo_screws();

!servo_support();
servo();
module servo() {
    w = servo_w();
    d = servo_d();
    h = servo_h();
    w2 = servo_w2();
    r = servo_r();
    r2 = 5/2;
    mr = 4.5/2;
    translate(-[w-r,d/2,15.5+2.5]) {
        color("Blue") {
            cube([w,d,h]);
        
            translate([-(w2-w)/2,0,15.5]) {
                difference() {
                    cube([w2,d,2.5]);
                    translate([2,d/2,0])
                    cylinder(h=5,r=1,center=true);
                    translate([w2-2,d/2,0])
                    cylinder(h=50,r=1,center=true);
                }
            }
            translate([w-r,d/2,h]) cylinder(h=4,r=r);
            translate([w-r*2,d/2,h]) cylinder(h=4,r=r2);
        }
        color("White") translate([w-r,d/2,h+4]) cylinder(h=3,r=mr);
    }
}
//servo();