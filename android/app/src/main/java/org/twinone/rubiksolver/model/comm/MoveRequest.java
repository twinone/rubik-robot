package org.twinone.rubiksolver.model.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Request to set a motor to a new position.
 */
public class MoveRequest extends Request {

    private final int motor;
    private final int position;

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

    public MoveRequest(int motor, int position) {
        if (position < 0 || position >= 2) throw new IllegalArgumentException("Invalid position specified");
        this.motor = motor;
        this.position = position;
    }

    @Override
    public byte getId() {
        return REQUEST_MOVE;
    }

    @Override
    public byte[] encode() {
        return new byte[] { REQUEST_MOVE, (byte) motor, (byte) position};
    }

    @Override
    public String toString() {
        return "MoveRequest{" + "motor=" + motor + ", position=" + position + '}';
    }
    
}
