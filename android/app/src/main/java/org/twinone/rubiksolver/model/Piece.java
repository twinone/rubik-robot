package org.twinone.rubiksolver.model;

/**
 * Created by twinone on 7/9/15.
 */
public interface Piece {
    public static final int CENTER = 1;
    public static final int EDGE = 2;
    public static final int CORNER = 3;

    // Number of stickers on this piece
    public int getSize();
    public int getType(); // alias for getSize()

    /**
     * @return An int array with the stickers of this piece.
     * The stickers are in clockwise order.
     */
    public int[] getStickers();

    /**
     * @return The face containing the first sticker of this {@link Piece}
     */
    public int getOrientation();
}
