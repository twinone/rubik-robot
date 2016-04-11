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
    public static final byte REQUEST_MOVE = 0x11;
    public static final byte REQUEST_WRITE = 0x12;

    public abstract byte getId();

    public abstract byte[] encode();

}
