package org.twinone.rubiksolver.robot.comm;

/**
 * Exception thrown when the backend returned an error response.
 */
public class FailedResponseException extends Exception {

    protected final Response response;

    public FailedResponseException(Response response) {
        super("Failed response: " + response.toString());
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

}
