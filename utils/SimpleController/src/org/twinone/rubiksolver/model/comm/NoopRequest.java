package org.twinone.rubiksolver.model.comm;

/**
 * No-op request.
 */
public class NoopRequest extends Request {

    public NoopRequest() {
    }

    @Override
    public byte getId() {
        return REQUEST_NOOP;
    }

    @Override
    public byte[] encode() {
        return new byte[] { REQUEST_NOOP };
    }

}
