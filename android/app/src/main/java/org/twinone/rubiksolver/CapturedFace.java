package org.twinone.rubiksolver;

import android.graphics.Color;

/**
 * Created by twinone on 6/20/15.
 */
public class CapturedFace {

    public final int size;
    // Colors of this CapturedFace
    public final double[][][] m;

    public CapturedFace(int size) {
        this.size = size;
        this.m = new double[size][size][3];
    }

    public int getColor(int i, int j) {
        double[] c = m[i][j];
        return Color.rgb((int) c[0], (int) c[1], (int) c[2]);
    }

}
