package org.twinone.rubiksolver.model.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Request to set a motor to a new position.
 */
public class MoveRequest extends Request {

    public static final int SIDE_RIGHT = 0;
    public static final int SIDE_UP = 1;
    public static final int SIDE_LEFT = 2;
    public static final int SIDE_DOWN = 3;

    public static final int MOTOR_GRIP = 0;
    public static final int MOTOR_ROTATION = 1;

    private final int side;
    private final int motor;
    private final int position;

    public int getSide() {
        return side;
    }

    public int getMotor() {
        return motor;
    }

    /**
     * position to move to:
     * - MOTOR_GRIP: 0 -> open, 1 -> grabbing
     * - MOTOR_ROTATION: 0 -> vertical, 1 -> horizontal
     * @return new position for motor
     */
    public int getPosition() {
        return position;
    }

    public MoveRequest(int side, int motor, int position) {
        if (side < 0 || side >= 4) throw new IllegalArgumentException("Invalid side specified");
        if (motor < 0 || motor >= 2) throw new IllegalArgumentException("Invalid motor specified");
        if (position < 0 || position >= 2) throw new IllegalArgumentException("Invalid position specified");
        this.side = side;
        this.motor = motor;
        this.position = position;
    }

    @Override
    public byte getId() {
        return REQUEST_MOVE;
    }

    @Override
    public byte[] encode() {
        return new byte[] { REQUEST_MOVE, (byte) (side << 1 | motor), (byte) position};
    }

}
