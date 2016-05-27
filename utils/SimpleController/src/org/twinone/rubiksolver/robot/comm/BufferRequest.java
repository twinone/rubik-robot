package org.twinone.rubiksolver.robot.comm;

/**
 * Request to wait until a number of bytes is available
 * to read immediately (not including this request bytes).
 */
public class BufferRequest extends Request {

    private final int peek;

    public BufferRequest(int peek) {
        if (peek < 0 || peek >= 0x100) throw new IllegalArgumentException();
        this.peek = peek;
    }

    public int getPeek() {
        return peek;
    }

    @Override
    public byte getId() {
        return REQUEST_BUFFER;
    }

    @Override
    public byte[] encode() {
        // FIXME: is the cast to byte okay, since peek can be >= 128?
        return new byte[] { REQUEST_BUFFER, (byte) peek};
    }

    @Override
    public String toString() {
        return "BufferRequest{" + "peek=" + peek + '}';
    }

}
