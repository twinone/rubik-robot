package org.twinone.rubiksolver.util;

import android.graphics.Color;
import android.util.Log;

import org.twinone.rubiksolver.model.Sticker;
import org.twinone.rubiksolver.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by twinone on 27/05/16.
 */
public class StickerSorter {

    public static String getState(List<Sticker> stickers) {
        return getState(stickers, null);
    }

    /**
     * Get the state of the cube and the average color of each face
     * @param stickers The input stickers
     * @param colorList The output colors will be added here in order (list will be cleared)
     * @return
     */
    public static String getState(List<Sticker> stickers, List<Integer> colorList) {
        if (colorList != null) colorList.clear();

        int ss = 9;
        List<List<Sticker>> groups = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            List<Sticker> l = new ArrayList<>();
            groups.add(0, l);
            // Add each center to its groups
            l.add(stickers.remove(4 + i * ss));
        }


        while (!stickers.isEmpty()) {
            Sticker candidate = null;
            List<Sticker> candidateList = null;
            double candidateDst = 0;

            for (List<Sticker> l : groups) {
                if (l.size() == ss) continue;
                int avg = average(l);

                Sticker st = getCandidate(stickers, avg);
                double dst = ColorUtil.colorDistance(st.color, avg);
                if (dst < candidateDst || candidate == null) {
                    candidate = st;
                    candidateDst = dst;
                    candidateList = l;
                }
            }

            //Log.d("Sort", "Adding sticker face=" + candidate.position / 9 + ", pos=" + candidate.position % 9 + " to list #" + groups.indexOf(candidateList));
            candidateList.add(candidate);
            stickers.remove(candidate);
        }

        char[] state = new char[6 * ss];
        int i = 0;
        for (List<Sticker> l : groups) {
            for (Sticker s : l) {
                state[s.position] = "ULFRBD".charAt(i);
            }
            if (colorList != null) colorList.add(average(l));
            i++;
        }
        return new String(state);
    }

    public static Sticker getCandidate(List<Sticker> stickers, int color) {
        double minDst = 0;
        Sticker res = null;
        for (Sticker s : stickers) {
            double dst = ColorUtil.colorDistance(color, s.color);
            if (dst < minDst || res == null) {
                minDst = dst;
                res = s;
            }
        }
        return res;
    }

    /**
     * @return the average color of this list of stickers
     */
    public static int average(List<Sticker> l) {
        int s = l.size();
        double r = 0;
        double g = 0;
        double b = 0;
        for (int i = 0; i < s; i++) {
            int c = l.get(i).color;
            int pr = Color.red(c);
            int pg = Color.green(c);
            int pb = Color.blue(c);
            r += pr * pr;
            g += pg * pg;
            b += pb * pb;
        }
        r = Math.sqrt(r / (double) s);
        g = Math.sqrt(g / (double) s);
        b = Math.sqrt(b / (double) s);
        return Color.rgb((int) r, (int) g, (int) b);
    }
}
