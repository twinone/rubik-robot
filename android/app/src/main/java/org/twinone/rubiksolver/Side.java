package org.twinone.rubiksolver;

/**
 * Created by twinone on 6/20/15.
 */
public class Side {

    public final int size;
    public final int[][] m;

    public Side(int size) {
        this.size = size;
        this.m = new int[size][size];
    }

}
