package org.twinone.rubiksolver.model;

import org.twinone.rubiksolver.model.comm.DelayRequest;
import org.twinone.rubiksolver.model.comm.Request;
import org.twinone.rubiksolver.model.comm.WriteRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maps algorithm moves to backend requests.
 */
public class SimpleRobotMapper {

    //FIXME: accessors for this
    
    protected int[] calibrationOffset = {
        155,  28,
        157, 150,
        118, 154,
        145,  16,
    };

    // Gripper-specific
    protected int gripAngle = +20;
    protected int ungripAngle = -40;

    // Rotation-specific
    protected int turnAngle = 142;
    protected int overshootAngle = 20;
    protected int recoverAngle = -7;
    

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
        if ("RBLFXZ".indexOf(move.face) != -1)
            return new AlgorithmMove[] {move};

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

        throw new IllegalArgumentException("Unknown move");
    }

    protected short delayPerGrip = 1000;
    protected short delayPerUngrip = 1000;
    protected short delayPerRotation = 1000;
    protected short delayPerFace = 1000;
    protected short delayPerRecover = 500;

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
     * @param move Move to map
     */
    public void map(List<Request> requests, AlgorithmMove move) {
        List<Request[]> chunks = new ArrayList<>();
        boolean forward = !move.reverse;

        // Cube rotations
        if (move.face == 'X' || move.face == 'Z') {
            boolean axis = !(move.face == 'X');

            // First chunk: ungrab the other axis so we can rotate the cube
            chunks.add(gripAxis(!axis, forward ? false : true));
            // Second chunk: rotate the cube with the main axis
            chunks.add(rotateAxis(axis, forward ? 1 : 0));
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
            chunks.add(new Request[] {
                    rotateSide(side, forward ? 1 : 0, overshootAngle),
                    new DelayRequest(calculateDelay(false, false, false, true, false)),
                    rotateSide(side, forward ? 1 : 0, recoverAngle),
                    new DelayRequest(calculateDelay(false, false, false, false, true)),
            });
            // Second chunk: ungrab the side to reset
            chunks.add(new Request[] {
                    gripSide(side, forward ? false : true, 0),
                    new DelayRequest(calculateDelay(false, true, false, false, false)),
            });
            // Third chunk: reset gripper back into position
            chunks.add(new Request[] {
                    rotateSide(side, forward ? 0 : 1, 0),
                    new DelayRequest(calculateDelay(false, false, false, true, false)),
            });
            // Fourth chunk: grab face again
            chunks.add(new Request[] {
                    gripSide(side, forward ? true : false, 0),
                    new DelayRequest(calculateDelay(true, false, false, false, false)),
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
     * @return List of backend requests
     */
    public List<Request> map(Iterable<AlgorithmMove> algorithm) {
        List<Request> requests = new ArrayList<>();
        for (AlgorithmMove rootMove : algorithm)
            for (AlgorithmMove move : preMap(rootMove))
                map(requests, move);
        return requests;
    }

}
