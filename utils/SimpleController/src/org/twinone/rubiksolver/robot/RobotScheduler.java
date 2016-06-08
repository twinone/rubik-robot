/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.twinone.rubiksolver.robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.twinone.rubiksolver.robot.comm.FailedResponseException;
import org.twinone.rubiksolver.robot.comm.Packet;
import org.twinone.rubiksolver.robot.comm.Request;
import org.twinone.rubiksolver.robot.comm.Response;
import org.twinone.rubiksolver.robot.comm.ResumeRequest;

/**
 * Sends chunks of requests to the backend, checking the status of each request
 * as it completes, sending resumes as necessary, etc.
 */
public class RobotScheduler implements AutoCloseable {

    public interface ChunkListener {
        void requestBegin(int i, Request req);

        void requestComplete(int i, Request req);

        void chunkFailed(int i, Request req, Response res);

        void chunkComplete();
    }

    public static abstract class ChunkAdapter implements ChunkListener {
        @Override
        public void requestBegin(int i, Request req) {
        }

        @Override
        public void requestComplete(int i, Request req) {
        }

        @Override
        public void chunkFailed(int i, Request req, Response res) {
        }

        @Override
        public void chunkComplete() {
        }
    }

    protected static class ChunkEntry {
        Iterable<Request> requests;
        ChunkListener listener;

        public ChunkEntry(Iterable<Request> requests, ChunkListener listener) {
            this.requests = requests;
            this.listener = listener;
        }
    }

    private final InputStream input;
    private final OutputStream output;
    private final Thread runner;
    private final int sendAhead;
    private SynchronousQueue<ChunkEntry> queue = new SynchronousQueue<>();

    public RobotScheduler(InputStream input, OutputStream output, int sendAhead) {
        this.input = input;
        this.output = output;
        this.sendAhead = sendAhead;
        this.runner = new Thread(new Runnable() {
            @Override
            public void run() {
                schedulerThread();
            }
        });
        runner.start();
    }

    public RobotScheduler(InputStream input, OutputStream output) {
        this(input, output, 10);
    }

    @Override
    public void close() throws IOException {
        try {
            queue.put(new ChunkEntry(null, null));
            input.close();
            output.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(RobotScheduler.class.getName()).log(Level.SEVERE, null, ex); //FIXME
        }
    }

    public void put(Iterable<Request> requests, ChunkListener listener) throws InterruptedException {
        if (requests == null) throw new IllegalArgumentException();
        queue.put(new ChunkEntry(requests, listener));
    }

    public boolean offer(Iterable<Request> requests, ChunkListener listener) {
        if (requests == null) throw new IllegalArgumentException();
        return queue.offer(new ChunkEntry(requests, listener));
    }

    public void put(Request request) throws InterruptedException {
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        queue.put(new ChunkEntry(requests, null));
    }

    private void schedulerThread() {
        try {
            ChunkEntry chunk;
            while ((chunk = queue.take()).requests != null)
                try {
                    processChunk(chunk);
                } catch (IOException | FailedResponseException ex) {
                    Logger.getLogger(RobotScheduler.class.getName()).log(Level.SEVERE, null, ex);//TODO
                }
        } catch (InterruptedException ex) {
            Logger.getLogger(RobotScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void processChunk(ChunkEntry chunk) throws IOException, FailedResponseException {
        Queue<Request> buffer = new ArrayBlockingQueue<>(sendAhead);
        Iterator<Request> requests = chunk.requests.iterator();

        // Send requests until we fill the buffer
        Request toSend = null;
        while (requests.hasNext() && buffer.offer(toSend = requests.next())) {
            Packet.write(output, toSend);
            toSend = null;
        }

        // Read responses while sending rest of the requests
        int idx = 0;
        while (toSend != null || !buffer.isEmpty()) {
            // Dequeue request
            Request toCheck = buffer.remove();
            if (chunk.listener != null) chunk.listener.requestBegin(idx, toCheck);
            Response response = Packet.readResponse(input);
            if (response == null || !response.isOk()) {
                if (chunk.listener != null) chunk.listener.chunkFailed(idx, toCheck, response);
                Packet.write(output, new ResumeRequest());
                Packet.checkResponse(input);
                return;
            }
            if (chunk.listener != null) chunk.listener.requestComplete(idx++, toCheck);

            // Enqueue request
            if (toSend != null) {
                buffer.add(toSend);
                Packet.write(output, toSend);
                toSend = null;
                if (requests.hasNext()) toSend = requests.next();
            }
        }

        if (chunk.listener != null) chunk.listener.chunkComplete();
    }

}
