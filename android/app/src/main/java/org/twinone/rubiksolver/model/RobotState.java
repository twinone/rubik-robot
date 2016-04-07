package org.twinone.rubiksolver.model;

import android.support.v4.widget.ListViewAutoScrollHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xavier on 06/04/16.
 */
public class RobotState {

    // Low level info
    public boolean grips[] = {false, false, false, false};
    public int positions[] = {0, 0, 0, 0};

    /**
     * Update the state as a result of a certain operation.
     * @param operation
     */
    public void update(RobotOperation operation) {
        if (operation.motor == RobotOperation.MOTOR_GRIP)
            grips[operation.side] = operation.position != 0;
        else
            positions[operation.side] = operation.position;

        // More intelligent code here
    }

    /**
     * This mapper assumes (and leaves) the cube in such an orientation that sides
     * 0, 1, 2 and 3 have the faces R, U, L, D respectively, all sides are gripped
     * and at position 0.
     *
     * TODO: implement XYZ again
     * FIXME: move this out of model
     * FIXME: change reference to RBLF
     *
     * @param algorithm Algorithm to translate
     * @return List of servo movements
     */
    public static List<RobotOperation> translateInAVerySimpleButHopefullyFunctionalWay(Iterable<AlgorithmMove> algorithm) {
        List<RobotOperation> ops = new ArrayList<>();
        List<Character> faces = new ArrayList<Character>();
        Collections.addAll(faces, 'R', 'U', 'L', 'D');
        for (AlgorithmMove move : algorithm) {
            if (faces.indexOf(move.face) == -1) {
                // We first need to rotate the cube to make face available

                // -> ungrip pair X
                ops.add(new RobotOperation(0, RobotOperation.MOTOR_GRIP, 0));
                ops.add(new RobotOperation(2, RobotOperation.MOTOR_GRIP, 0));
                // -> rotate cube with pair Y
                ops.add(new RobotOperation(1, RobotOperation.MOTOR_ROTATION, +1));
                ops.add(new RobotOperation(3, RobotOperation.MOTOR_ROTATION, -1));
                // -> grip pair X again
                ops.add(new RobotOperation(0, RobotOperation.MOTOR_GRIP, 1));
                ops.add(new RobotOperation(2, RobotOperation.MOTOR_GRIP, 1));
                // -> ungrip pair Y, return to original position and grip
                ops.add(new RobotOperation(1, RobotOperation.MOTOR_GRIP, 0));
                ops.add(new RobotOperation(3, RobotOperation.MOTOR_GRIP, 0));
                ops.add(new RobotOperation(1, RobotOperation.MOTOR_ROTATION, 0));
                ops.add(new RobotOperation(3, RobotOperation.MOTOR_ROTATION, 0));
                ops.add(new RobotOperation(1, RobotOperation.MOTOR_GRIP, 1));
                ops.add(new RobotOperation(3, RobotOperation.MOTOR_GRIP, 1));

                switch (faces.get(0)) {
                    case 'L':
                        faces.set(0, 'B');
                        faces.set(2, 'F');
                        break;
                    case 'F':
                        faces.set(0, 'L');
                        faces.set(2, 'R');
                        break;
                    case 'R':
                        faces.set(0, 'F');
                        faces.set(2, 'B');
                        break;
                    case 'B':
                        faces.set(0, 'R');
                        faces.set(2, 'L');
                        break;
                }
            }

            // Locate the side the face is on, rotate it
            int side = faces.indexOf(move.face);
            ops.add(new RobotOperation(side, RobotOperation.MOTOR_ROTATION, move.reverse ? -1 : +1));
            ops.add(new RobotOperation(side, RobotOperation.MOTOR_GRIP, 0));
            ops.add(new RobotOperation(side, RobotOperation.MOTOR_ROTATION, 0));
            ops.add(new RobotOperation(side, RobotOperation.MOTOR_GRIP, 1));
        }

        return ops;
    }

}
