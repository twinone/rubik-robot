package org.twinone.rubiksolver.robot;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Reverse an algorithm in place.
     * @param algorithm Algorithm to reverse
     */
    public static void reverse(List<AlgorithmMove> algorithm) {
        Collections.reverse(algorithm);
        for (AlgorithmMove move : algorithm)
            move.reverse = !move.reverse;
    }

    public static String format(Iterable<AlgorithmMove> algorithm) {
        String tokens = "";
        for (AlgorithmMove move : algorithm) {
            String token = String.valueOf(move.face);
            if (move.reverse) token += "'";
            if (tokens.length() > 0) tokens += " ";
            tokens += token;
        }
        return tokens;
    }

}
