package org.twinone.rubiksolver.model.comm;

/**
 * Request to set a motor to a new position.
 *
 * Unless {@ref MoveRequest}, this request specifies a raw
 * position in degrees to be written to the servo. It's meant
 * to be used primarily for debugging and calibration.
 */
public class WriteRequest extends Request {

    private final int side;
    private final int motor;
    private final int position;

    public int getSide() {
        return side;
    }

    public int getMotor() {
        return motor;
    }

    public int getPosition() {
        return position;
    }

    public WriteRequest(int side, int motor, int position) {
        if (side < 0 || side >= 4) throw new IllegalArgumentException("Invalid side specified");
        if (motor < 0 || motor >= 2) throw new IllegalArgumentException("Invalid motor specified");
        if (position < 0 || position >= 180) throw new IllegalArgumentException("Invalid position specified");
        this.side = side;
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
        return new byte[] { REQUEST_WRITE, (byte) (side << 1 | motor), (byte) position};
    }

}
