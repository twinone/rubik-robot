#!/bin/bash

PARTS="arm_left arm_right grip_top grip_bottom grip_pad grip_cylinder holder_top holder_bottom servo_gear grip_gear foot"
OPENSCAD="`which openscad`"
QUALITY=50

if [[ $OPENSCAD == "" ]]; then
	echo "please install openscad first: apt-get install openscad"
	exit
fi

if [[ -n "$1" ]]; then
	PARTS=$1
fi

for part in $PARTS; do
	echo $OPENSCAD -D part=\"$part\" -D '$fn='$QUALITY -o build/$part.stl export.scad
	$OPENSCAD -D part=\"$part\" -D '$fn='$QUALITY -o build/$part.stl export.scad
done
