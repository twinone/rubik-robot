package org.twinone.rubiksolver.model.comm;

/**
 * Backend request used to float a specific motor (stop applying force).
 */
public class DetachRequest extends Request {

    private final int motor;

    public int getMotor() {
        return motor;
    }

    public DetachRequest(int motor) {
        this.motor = motor;
    }

    @Override
    public byte getId() {
        return REQUEST_DETACH;
    }

    @Override
    public byte[] encode() {
        return new byte[] { REQUEST_DETACH, (byte) motor };
    }

    @Override
    public String toString() {
        return "DetachRequest{" + "motor=" + motor + '}';
    }

}
