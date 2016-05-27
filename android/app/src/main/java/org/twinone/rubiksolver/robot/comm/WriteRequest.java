package org.twinone.rubiksolver.robot.comm;

/**
 * Request to set a motor to a new position.
 *
 * Unless {@ref MoveRequest}, this request specifies a raw
 * position in degrees to be written to the servo. It's meant
 * to be used primarily for debugging and calibration.
 */
public class WriteRequest extends Request {

    private final int motor;
    private final int position;

    public int getMotor() {
        return motor;
    }

    public int getPosition() {
        return position;
    }

    public WriteRequest(int motor, int position) {
        if (position < 0 || position > 180) throw new IllegalArgumentException("Invalid position specified");
        this.motor = motor;
        this.position = position;
    }

    @Override
    public byte getId() {
        return REQUEST_WRITE;
    }

    @Override
    public byte[] encode() {
        // FIXME: is the cast to byte okay, since position can be >= 128?
        return new byte[] { REQUEST_WRITE, (byte) motor, (byte) position};
    }

    @Override
    public String toString() {
        return "WriteRequest{" + "motor=" + motor + ", position=" + position + '}';
    }

}
