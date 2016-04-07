package org.twinone.rubiksolver.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a basic algorithm move.
 */
public class AlgorithmMove {
    public char face;
    public boolean reverse;

    public AlgorithmMove(char face, boolean reverse) {
        this.face = face;
        this.reverse = reverse;
    }

    public static List<AlgorithmMove> parse(String algorithm) {
        List<AlgorithmMove> moves = new ArrayList<>();
        while (true) {
            algorithm = algorithm.trim();
            if (algorithm.length() == 0) break;
            char face = algorithm.charAt(0);
            boolean reverse = false;
            if ("ULFRBDXYZMSE".indexOf(face) == -1) throw new IllegalArgumentException();
            algorithm = algorithm.substring(1);
            if (algorithm.length() > 0 && algorithm.charAt(0) == '\'') {
                algorithm = algorithm.substring(1);
                reverse = true;
            }
            moves.add(new AlgorithmMove(face, reverse));
        }
        return moves;
    }
}
