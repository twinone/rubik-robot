package org.twinone.rubiksolver.robot.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Request to delay for a specified number of milliseconds.
 */
public class DelayRequest extends Request {

    private final short delay;

    public DelayRequest(short delay) {
        if (delay <= 0) throw new IllegalArgumentException();
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public byte getId() {
        return REQUEST_DELAY;
    }

    @Override
    public byte[] encode() {
        ByteBuffer data = ByteBuffer.allocate(3);
        data.order(ByteOrder.BIG_ENDIAN);
        data.put(REQUEST_DELAY);
        data.putShort(delay);
        return data.array();
    }

    @Override
    public String toString() {
        return "DelayRequest{" + "delay=" + delay + '}';
    }
    
}
