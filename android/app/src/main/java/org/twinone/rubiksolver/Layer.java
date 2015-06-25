package org.twinone.rubiksolver;

/**
 * Created by twinone on 6/20/15.
 */
public class Layer {

    public static final int LAYER_FRONT = 0;
    public static final int LAYER_BACK = 1;
    public static final int LAYER_UP = 2;
    public static final int LAYER_DOWN = 3;
    public static final int LAYER_LEFT = 4;
    public static final int LAYER_RIGHT = 5;

    // Clients may choose not to support these inner layers,
    // in which case we'll send the commands with
    // only outer layers
    public static final int LAYER_STANDING = 6;
    public static final int LAYER_EQUATOR = 7;
    public static final int LAYER_MIDDLE = 8;

    public final int size;
    // Colors of this Layer
    public final int[][] m;

    public Layer(int size) {
        this.size = size;
        this.m = new int[size][size];
    }

}
