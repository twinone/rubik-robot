package org.twinone.rubiksolver.model.comm;

/**
 * Represents a request made to the backend.
 */
public abstract class Request {

    public static final byte REQUEST_NOOP = 0x00;
    public static final byte REQUEST_RESUME = 0x01;
    public static final byte REQUEST_DELAY = 0x02;
    public static final byte REQUEST_BUFFER = 0x03;

    public static final byte REQUEST_CALIBRATE = 0x10;
    public static final byte REQUEST_DETACH = 0x11;
    public static final byte REQUEST_WRITE = 0x12;

    public abstract byte getId();

    public abstract byte[] encode();
    
    
    public static final int SIDE_RIGHT = 0;
    public static final int SIDE_UP = 1;
    public static final int SIDE_LEFT = 2;
    public static final int SIDE_DOWN = 3;

    public static final int MOTOR_GRIP = 0;
    public static final int MOTOR_ROTATION = 1;
    
    public static int getMotor(int side, int motor) {
        if (side < 0 || side >= 4) throw new IllegalArgumentException("Invalid side specified");
        if (motor < 0 || motor >= 2) throw new IllegalArgumentException("Invalid motor specified");
        return (side << 1) | motor;
    }

}
