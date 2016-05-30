package org.twinone.rubiksolver.robot;

import org.twinone.rubiksolver.robot.AlgorithmMove;
import org.twinone.rubiksolver.robot.comm.DelayRequest;
import org.twinone.rubiksolver.robot.comm.Request;
import org.twinone.rubiksolver.robot.comm.WriteRequest;
import org.twinone.rubiksolver.robot.comm.DetachRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maps algorithm moves to backend requests.
 */
public class SimpleRobotMapper {
    public static class RequestTag {
        // Equivalent move executed in the cube
        AlgorithmMove move;
        // Requests that perform this move
        Request[] requests;
        // Time it takes for `move` to execute
        int time;
    }
    
    private static Request[] tag(List<RequestTag> tags, Request[] reqs, AlgorithmMove move) {
        if (tags != null) {
            int time = 0;
            for (Request req : reqs)
                if (req instanceof DelayRequest) time += ((DelayRequest) req).getDelay();
            RequestTag tag = new RequestTag();
            tag.requests = reqs;
            tag.move = move;
            tag.time = time;
            tags.add(tag);
        }
        return reqs;
    }

    //FIXME: accessors for this
    
    protected int[] calibrationOffset = {
        138,  46,
        146, 119,
         98, 123,
        150,  24,
    };

    // Gripper-specific
    protected int gripAngle = +30;
    protected int ungripAngle = -40;

    // Rotation-specific
    protected int turnAngle = 103;
    protected int[] overshootAngle = { 5, 5, 5, 5 };
    protected int recoverAngle = -10;


    public WriteRequest gripSide(int side, boolean grip, int offset) {
        int motor = Request.getMotor(side, Request.MOTOR_GRIP);
        int position = calibrationOffset[motor] + (grip ? gripAngle : ungripAngle);
        return moveMotor(motor, position + offset);
    }
    
    public WriteRequest rotateSide(int side, int pos, int offset) {
        int motor = Request.getMotor(side, Request.MOTOR_ROTATION);
        int position = calibrationOffset[motor];
        boolean reverse = (side == 1) || (side == 2);
        offset *= (pos != 0) ? +1 : -1;
        if (pos != 0) offset += turnAngle;
        return moveMotor(motor, position + offset * (reverse ? -1 : +1));
    }
    
    public static WriteRequest moveMotor(int motor, int pos) {
        return new WriteRequest(motor, Math.max(Math.min(pos, 180), 0));
    }
    
    /**
     * Map an algorithm move to other algorithm moves that can be mapped and are equivalent.
     * @return suitable algorithm moves
     */
    public static AlgorithmMove[] preMap(AlgorithmMove move) {
        if (move.face == 'Y') {
            List<AlgorithmMove> result = AlgorithmMove.parse("X Z' X'");
            result.get(1).reverse = !move.reverse;
            return result.toArray(new AlgorithmMove[0]);
        }

        if (move.face == 'U' || move.face == 'D') {
            List<AlgorithmMove> result = AlgorithmMove.parse("X' F X");
            if (move.face == 'D') result.get(1).face = 'B';
            result.get(1).reverse = move.reverse;
            return result.toArray(new AlgorithmMove[0]);
        }
        
        if ("RBLFXZ".indexOf(move.face) != -1)
            return new AlgorithmMove[] {move};

        throw new IllegalArgumentException("Unknown move");
    }

    protected short delayPerGrip = 250;
    protected short delayPerUngrip = 250;
    protected short delayPerRotation = 500;
    protected short delayPerFace = 800;
    protected short delayPerRecover = 200;

    public void setDelayPerGrip(short delayPerGrip) {
        this.delayPerGrip = delayPerGrip;
    }

    public void setDelayPerUngrip(short delayPerUngrip) {
        this.delayPerUngrip = delayPerUngrip;
    }

    public void setDelayPerRotation(short delayPerRotation) {
        this.delayPerRotation = delayPerRotation;
    }

    public void setDelayPerFace(short delayPerFace) {
        this.delayPerFace = delayPerFace;
    }

    public void setDelayPerRecover(short delayPerRecover) {
        this.delayPerRecover = delayPerRecover;
    }

    public short getDelayPerGrip() {
        return delayPerGrip;
    }

    public short getDelayPerUngrip() {
        return delayPerUngrip;
    }

    public short getDelayPerRotation() {
        return delayPerRotation;
    }

    public short getDelayPerFace() {
        return delayPerFace;
    }

    public short getDelayPerRecover() {
        return delayPerRecover;
    }

    public short calculateDelay(boolean grip, boolean ungrip, boolean rotation, boolean face, boolean recover) {
        short delay = 0;
        if (grip && delay < delayPerGrip) delay = delayPerGrip;
        if (ungrip && delay < delayPerUngrip) delay = delayPerUngrip;
        if (rotation && delay < delayPerRotation) delay = delayPerRotation;
        if (face && delay < delayPerFace) delay = delayPerFace;
        if (recover && delay < delayPerRecover) delay = delayPerRecover;
        return delay;
    }

    public SimpleRobotMapper() {
    }

    public DetachRequest detachGripper(int side) {
        return new DetachRequest(Request.getMotor(side, Request.MOTOR_GRIP));
    }

    public DetachRequest detachRotation(int side) {
        return new DetachRequest(Request.getMotor(side, Request.MOTOR_ROTATION));
    }

    /**
     * Generate a chunk of backend requests to grip (or ungrip) an axis.
     *
     * @param axis Axis to grip (false = horizontal, true = vertical)
     * @return Chunk of backend requests
     */
    public Request[] gripAxis(boolean axis, boolean gripped) {
        return new Request[] {
                gripSide(axis ? 1 : 0, gripped, 0),
                gripSide(axis ? 3 : 2, gripped, 0),
                new DelayRequest(calculateDelay(gripped, !gripped, false, false, false)),
        };
    }

    /**
     * Generate a chunk of backend requests to grip (or ungrip) an axis.
     *
     * @param axis Axis to grip (false = horizontal, true = vertical)
     * @return Chunk of backend requests
     */
    public Request[] rotateAxis(boolean axis, int position) {
        return new Request[] {
                rotateSide(axis ? 1 : 0, position, 0),
                rotateSide(axis ? 3 : 2, position, 0),
                new DelayRequest(calculateDelay(false, false, true, false, false)),
        };
    }

    /**
     * Map a single move directly to backend requests. This method assumes (and leaves) the
     * cube in such an orientation that sides 0, 1, 2 and 3 have the faces R, B, L, F
     * respectively, all sides are gripped and at position 0 (vertical).
     *
     * **Important:** This method will fail if the supplied move is not one of RBLFXZ or
     * their reverses. To map arbitrary moves, use {@link SimpleRobotMapper#map(Iterable)}
     *
     * @param requests Resulting requests will be appended to this list.
     * @param tags Optional list where request tags will be put
     * @param move Move to map
     */
    public void map(List<Request> requests, List<RequestTag> tags, AlgorithmMove move) {
        List<Request[]> chunks = new ArrayList<>();
        boolean forward = !move.reverse;

        // Cube rotations
        if (move.face == 'X' || move.face == 'Z') {
            boolean axis = !(move.face == 'X');

            // First chunk: ungrab the other axis so we can rotate the cube
            chunks.add(gripAxis(!axis, forward ? false : true));
            // Second chunk: rotate the cube with the main axis
            chunks.add(tag(tags, rotateAxis(axis, forward ? 1 : 0), move));
            // Third chunk: grab the cube with the other axis again
            chunks.add(gripAxis(!axis, forward ? true : false));
            // Fourth chunk: ungrab with the main axis so we can reset
            chunks.add(gripAxis(axis, forward ? false : true));
            // Fifth chunk: reset the main axis back into position
            chunks.add(rotateAxis(axis, forward ? 0 : 1));
            // Sixth chunk: grab with the main axis again
            chunks.add(gripAxis(axis, forward ? true : false));
        }

        // Face rotation
        int side = "RBLF".indexOf(move.face);
        if (side != -1) {
            if (side == 1 || side == 2) forward = !forward;
            
            // First chunk: rotate the face, overshooting, delay, then recover
            chunks.add(tag(tags, new Request[] {
                    rotateSide(side, forward ? 1 : 0, overshootAngle[side]),
                    new DelayRequest(calculateDelay(false, false, false, true, false)),
                    rotateSide(side, forward ? 1 : 0, recoverAngle),
                    new DelayRequest(calculateDelay(false, false, false, false, true)),
            }, move));
            // Second chunk: ungrab the side to reset
            chunks.add(new Request[] {
                    gripSide(side, forward ? false : true, 0),
                    new DelayRequest(calculateDelay(!forward, forward, false, false, false)),
            });
            // Third chunk: reset gripper back into position
            chunks.add(new Request[] {
                    rotateSide(side, forward ? 0 : 1, 0),
                    new DelayRequest(calculateDelay(false, false, true, false, false)),
            });
            // Fourth chunk: grab face again
            chunks.add(new Request[] {
                    gripSide(side, forward ? true : false, 0),
                    new DelayRequest(calculateDelay(forward, !forward, false, false, false)),
            });
        }

        if (!forward) Collections.reverse(chunks);
        for (Request[] chunk : chunks)
            Collections.addAll(requests, chunk);
    }

    /**
     * Map an algorithm to a sequence of backend requests.
     *
     * This method assumes (and leaves) the cube in such an orientation that sides
     * 0, 1, 2 and 3 have the faces R, B, L, F respectively, all sides are gripped
     * and at position 0 (vertical).
     *
     * @param algorithm Algorithm to translate
     * @param tags Optional list where request tags will be put
     * @return List of backend requests
     */
    public List<Request> map(Iterable<AlgorithmMove> algorithm, List<RequestTag> tags) {
        List<Request> requests = new ArrayList<>();
        for (AlgorithmMove rootMove : algorithm)
            for (AlgorithmMove move : preMap(rootMove))
                map(requests, tags, move);
        return requests;
    }

}
