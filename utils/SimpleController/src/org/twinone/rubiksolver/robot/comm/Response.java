package org.twinone.rubiksolver.robot.comm;

/**
 * Represents a response from the backend.
 * FIXME: migrate to multiple classes
 */
public class Response {

    public static final byte RESPONSE_OK = 0x00;
    public static final byte RESPONSE_INVALID_COMMAND = 0x01;
    public static final byte RESPONSE_INVALID_ARGUMENT = 0x02;

    protected final byte id;

    public Response(byte id) {
        this.id = id;
    }

    public Response() {
        this(RESPONSE_OK);
    }

    public byte getId() {
        return id;
    }

    public boolean isOk() {
        return getId() == RESPONSE_OK;
    }

    public static Response decode(byte[] data) {
        if (data.length != 1) throw new IllegalArgumentException("Invalid response size");
        return new Response(data[0]);
    }

}
