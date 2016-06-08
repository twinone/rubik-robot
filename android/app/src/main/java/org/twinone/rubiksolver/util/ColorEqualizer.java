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

    public static final double LUMINANCE_UPPER_BOUND = 100;
    public static final double MAX_LUMINANCE_SCALE_FACTOR = 2.5;
    public static final double CHROMA_UPPER_BOUND = 100;
    public static final double MAX_CHROMA_SCALE_FACTOR = 2;
    public static final double DISTANCE_CLOSE_THRESHOLD = 30;
    public static final double DISTANCE_OPEN_THRESHOLD = 20;

    public static void equalize(List<Integer> colors) {
        List<double[]> labColors = new ArrayList<>();
        for (int color : colors) {
            double[] labColor = ColorUtil.ColorToLAB(color);
            labColors.add(labColor);
            Log.i("equalizer", String.format("Color #%06X: %f %f %f", color, labColor[0], labColor[1], labColor[2]));
        }

        // LUMINANCE EQUALIZING

        // Calculate max luminance
        double max_luminance = 0;
        for (double[] color : labColors)
            if (color[0] > max_luminance) max_luminance = color[0];

        // Scale luminance as necessary
        double mapped_max_luminance = Math.max(max_luminance, LUMINANCE_UPPER_BOUND);
        double luminance_scale = Math.min(mapped_max_luminance / max_luminance, MAX_LUMINANCE_SCALE_FACTOR);
        for (double[] color : labColors) {
            color[0] *= luminance_scale;
        }

        // CHROMA EQUALIZING

        // Calculate max chroma
        double max_chroma = 0;
        for (double[] color : labColors) {
            double chroma = Math.sqrt(color[1]*color[1] + color[2]*color[2]);
            if (chroma > max_chroma) max_chroma = chroma;
        }

        // Scale chroma as necessary
        double mapped_max_chroma = Math.max(max_chroma, CHROMA_UPPER_BOUND);
        double chroma_scale = Math.min(mapped_max_chroma / max_chroma, MAX_CHROMA_SCALE_FACTOR);
        for (double[] color : labColors) {
            color[1] *= chroma_scale;
            color[2] *= chroma_scale;
        }

        // QUANTIZATION

        // Quantify colors if possible
        for (int candidate : TYPICAL_COLORS) {
            double[] labCandidate = ColorUtil.ColorToLAB(candidate);
            Log.d("equalize", String.format("Now evaluating #%06X: %f %f %f", candidate, labCandidate[0], labCandidate[1], labCandidate[2]));
            double minDistance = 0;
            int colorIdx = -1;
            for (int i = 0; i < colors.size(); i++) {
                double distance = ColorUtil.colorDistanceCore(labCandidate, labColors.get(i));
                Log.d("equalize", String.format("distance to #%06X: %f", colors.get(i), distance));
                if (distance > DISTANCE_CLOSE_THRESHOLD) continue;
                if (colorIdx == -1 || minDistance > distance) { minDistance = distance; colorIdx = i; }
            }
            if (colorIdx == -1) continue;
            // We have a matching color, check other colors don't match
            boolean colorsAreOut = true;
            for (int i = 0; i < colors.size(); i++)
                if (colorIdx != i && ColorUtil.colorDistanceCore(labCandidate, labColors.get(i)) < DISTANCE_OPEN_THRESHOLD) {
                    colorsAreOut = false;
                    break;
                }
            // Replace color with candidate
            if (colorsAreOut) {
                Log.d("equalizer", "Using!");
                labColors.set(colorIdx, labCandidate);
            }
        }

        // Render colors
        Log.i("equalizer", "Finished colors");
        for (int i = 0; i < colors.size(); i++) {
            int[] rgb = ColorUtil.LABtoRGB(labColors.get(i));
            colors.set(i, Color.rgb(rgb[0], rgb[1], rgb[2]));
            double[] labColor = labColors.get(i);
            Log.i("equalizer", String.format("Color #%06X: %f %f %f", colors.get(i), labColor[0], labColor[1], labColor[2]));
        }
    }

}
