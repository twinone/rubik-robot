package org.twinone.rubiksolver.robot.comm;

/**
 * Request to resume processing requests after a failed response.
 */
public class ResumeRequest extends Request {

    public ResumeRequest() {
    }

    @Override
    public byte getId() {
        return REQUEST_RESUME;
    }

    @Override
    public byte[] encode() {
        return new byte[] { REQUEST_RESUME };
    }

    @Override
    public String toString() {
        return "ResumeRequest{" + '}';
    }

}
