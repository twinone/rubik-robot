package org.twinone.rubiksolver.robot.comm;

import java.util.ArrayList;
import java.util.List;

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


    /**
     * @return all motors of the specified type
     */
    public static List<Integer> getAll(int motor) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 4; i++) res.add(i << 1 | motor);
        return res;
    }

    /**
     * @return all motors of the specified side
     */
    public static List<Integer> getSide(int side) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 2; i++) res.add(side << 1 | i);
        return res;
    }


}
