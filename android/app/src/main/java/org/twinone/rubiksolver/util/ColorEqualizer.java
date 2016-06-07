package org.twinone.rubiksolver.util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavier on 07/06/16.
 */
public class ColorEqualizer {

    public static final int[] TYPICAL_COLORS = {
            Color.parseColor("#ff0000"),
            Color.parseColor("#00ff00"),
            Color.parseColor("#0000ff"),
            Color.parseColor("#ffffff"),
            Color.parseColor("#ffff00"),
            Color.parseColor("#ff7f00"),
            Color.parseColor("#000000"),
    };

    public static final double LUMINANCE_UPPER_BOUND = 95;
    public static final double DISTANCE_CLOSE_THRESHOLD = 10;
    public static final double DISTANCE_OPEN_THRESHOLD = 20;


    public static void equalize(List<Integer> colors) {
        List<double[]> labColors = new ArrayList<>();
        for (int color : colors)
            labColors.add(ColorUtil.ColorToLAB(color));

        // Calculate max luminance
        double max_luminance = 0;
        for (double[] color : labColors)
            if (color[0] > max_luminance) max_luminance = color[0];
        double mapped_max_luminance = Math.max(max_luminance, LUMINANCE_UPPER_BOUND);

        // Add as necessary to the luminance
        for (double[] color : labColors)
            color[0] += mapped_max_luminance - max_luminance;

        // Quantify colors if possible
/*        for (int candidate : TYPICAL_COLORS) {
            double[] labCandidate = ColorUtil.ColorToLAB(candidate);
            double minDistance = 0;
            int colorIdx = -1;
            for (int i = 0; i < colors.length; i++) {
                double distance = ColorUtil.colorDistanceCore(labCandidate, labColors.get(i));
                if (distance > DISTANCE_CLOSE_THRESHOLD) continue;
                if (colorIdx == -1 || minDistance > distance) { minDistance = distance; colorIdx = i; }
            }
            if (colorIdx == -1) continue;
            // We have a matching color, check other colors don't match
            boolean colorsAreOut = true;
            for (int i = 0; i < colors.length; i++)
                if (colorIdx != i && ColorUtil.colorDistanceCore(labCandidate, labColors.get(i)) < DISTANCE_OPEN_THRESHOLD) {
                    colorsAreOut = false;
                    break;
                }
            // Swap colors
            if (colorsAreOut)
                labColors.set(colorIdx, labCandidate);
        }*/

        // Render colors
        for (int i = 0; i < colors.size(); i++) {
            int[] rgb = ColorUtil.LABtoRGB(labColors.get(i));
            colors.set(i, Color.rgb(rgb[0], rgb[1], rgb[2]));
        }
    }

}
