package org.twinone.rubiksolver.robot.comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class to read / write packets to / from the backend.
 */
public class Packet {

    private Packet() {}

    /**
     * Read next packet from input stream.
     * @return next packet data, or null if EOF reached
     */
    public static byte[] read(InputStream stream) throws IOException {
        int length = stream.read();
        if (length == -1) return null;
        byte[] data = new byte[length];
        int read = 0;
        while (read < length) {
            int r = stream.read(data, read, length - read);
            if (r == -1) throw new IllegalArgumentException("Stream ended unexpectedly");
            read += r;
        }
        return data;
    }

    /**
     * Read next packet as a response, from the input stream, and verify success.
     * @return next decoded response, or null if EOF reached
     */
    public static Response readResponse(InputStream stream) throws IOException {
        byte[] data = read(stream);
        if (data == null) return null;
        return Response.decode(data);
    }
    
    /**
     * Read next packet as a response and check it's successful.
     * If it's a failed response, an exception will be thrown.
     * @param stream
     * @throws IOException
     * @throws FailedResponseException 
     */
    public static void checkResponse(InputStream stream) throws IOException, FailedResponseException {
        Response response = readResponse(stream);
        if (response == null || !response.isOk()) throw new FailedResponseException(response);
    }

    /**
     * Write packet to output stream. Does not flush the stream.
     */
    public static void write(OutputStream stream, byte[] data) throws IOException {
        if (data.length >= 0x100) throw new IllegalArgumentException("Packet data is too long.");
        stream.write(data.length);
        stream.write(data);
    }

    /**
     * Write request to output stream. Does not flush the stream.
     */
    public static void write(OutputStream stream, Request request) throws IOException {
        write(stream, request.encode());
    }

    /**
     * Utility method to write a sequence of requests to output stream,
     * preceded by a BufferRequest of appropiate peek to ensure they are performed in sync.
     *
     * @throws IllegalArgumentException If there are too many requests to buffer.
     */
    public static void writeWithPeek(OutputStream stream, Iterable<Request> requests) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        for (Request request : requests)
            write(temp, request);
        byte[] data = temp.toByteArray();
        write(temp, new BufferRequest(data.length));
        stream.write(data);
    }

}
