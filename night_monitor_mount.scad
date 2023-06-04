$fn = 45;

atomMount();

module atomMount() {
    difference() {
        translate([0, 0, 0]) {
            translate([3.5, 0, 0]) cube([16, 8, 8]);
            translate([3.5, 4, 0]) {
                difference() {
                    cylinder(d = 8, h = 8);
                    translate([0, -5, 0]) cube([9, 9, 9]);
                }
            }
            translate([19.5, 4, 0]) {
                difference() {
                    cylinder(d = 8, h = 8);
                    translate([0, 5, 0]) cube([9, 9, 9]);
                }
            }
        }
        translate([3.5, 4, -0.5]) {
            cylinder(d = 5, h = 9);
            cylinder(d = 6, h = 1.5);
            translate([0, 0, 7.1]) cylinder(d = 6, h = 1.5);
        }
        translate([19.5, 4, -0.5]) {
            cylinder(d = 5, h = 9);
            cylinder(d = 6, h = 1.5);
            translate([0, 0, 7.1]) cylinder(d = 6, h = 1.5);
        }
    }
    translate([10.5, -4, 0]) {
        difference() {
            cube([6, 8, 18]);
            translate([3, 9, 15]) rotate([90, 0, 0]) cylinder(d = 2.5, h = 20);
            translate([3, 9, 15]) rotate([90, 0, 0]) cylinder(d = 4.5, h = 6);
        }
    }
    
}