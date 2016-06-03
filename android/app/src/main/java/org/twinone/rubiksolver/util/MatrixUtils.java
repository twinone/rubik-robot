package org.twinone.rubiksolver.util;

/**
 * Created by xavier on 02/06/16.
 */
public class MatrixUtils {

    public static void rotate(double[][][] matrix, int n) {
        for (int layer = 0; layer < n / 2; ++layer) {
            int first = layer;
            int last = n - 1 - layer;
            for(int i = first; i < last; ++i) {
                int offset = i - first;
                double[] top = matrix[first][i]; // save top

                // left -> top
                matrix[first][i] = matrix[last-offset][first];

                // bottom -> left
                matrix[last-offset][first] = matrix[last][last - offset];

                // right -> bottom
                matrix[last][last - offset] = matrix[i][last];

                // top -> right
                matrix[i][last] = top; // right <- saved top
            }
        }
    }

}
