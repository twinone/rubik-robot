package org.twinone.rubiksolver.robot.comm;

import java.util.ArrayList;
import java.util.List;

/**
 * Backend request used to float a specific motor (stop applying force).
 */
public class DetachRequest extends Request {

    public static final List<DetachRequest> DETACH_ALL = new ArrayList<>();
    static {
        for (int i : getAll(MOTOR_GRIP)) DETACH_ALL.add(new DetachRequest(i));
        for (int i : getAll(MOTOR_ROTATION)) DETACH_ALL.add(new DetachRequest(i));
    }

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
