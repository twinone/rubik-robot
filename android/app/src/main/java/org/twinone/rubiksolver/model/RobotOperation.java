package org.twinone.rubiksolver.model;

/**
 * Represents a robot operation (currently, only motor movement).
 */
public class RobotOperation {

    public static final int MOTOR_GRIP = 1;
    public static final int MOTOR_ROTATION = 2;

    // 0 means right, 1 means up, etc.
    public int side;

    // which motor to move
    public int motor;

    // position to move to:
    //  - MOTOR_GRIP: 0 -> open, 1 -> grabbing
    //  - MOTOR_ROTATION: 0 -> vertical, 1 -> 90 deg move, -1 -> reverse 90 deg move, 2 -> vertical inverse
    public int position;

    public RobotOperation(int side, int motor, int position) {
        this.side = side;
        this.motor = motor;
        this.position = position;
    }

}
