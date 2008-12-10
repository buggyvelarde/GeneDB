package org.genedb.util;

import java.awt.Color;
import java.awt.image.IndexColorModel;

public class ColorUtils {

    /**
     * Create a minimal indexed colour model that includes the
     * specified colours.
     *
     * @param colors the colours to include
     * @return the computed IndexColorModel
     */
    public static IndexColorModel colorModelFor(Color... colors) {
        int len = 1 + colors.length;
        int bits = 1, twoToBits = 2;
        while (twoToBits < len) {
            bits++;
            twoToBits *= 2;
        }

        byte[] reds   = new byte[len];
        byte[] greens = new byte[len];
        byte[] blues  = new byte[len];
        byte[] alphas = new byte[len];

        reds[0] = greens[0] = blues[0] = alphas[0] = 0; // transparent "colour"
        for (int i=1; i < len; i++) {
            reds[i]   = (byte) colors[i-1].getRed();
            greens[i] = (byte) colors[i-1].getGreen();
            blues[i]  = (byte) colors[i-1].getBlue();
            alphas[i] = (byte) colors[i-1].getAlpha();
        }

        return new IndexColorModel(bits, 1 + colors.length, reds, greens, blues, alphas);
    }

}
