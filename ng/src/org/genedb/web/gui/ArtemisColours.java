package org.genedb.web.gui;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Traditional mapping from internal colour numbers to real Color
 *
 * @author kmr
 */
public class ArtemisColours {
    /**
     * The number of elements in the mapping
     */
    private static int numCols = 104;

    /**
     * The list of colours. The mapping is performed by a direct index lookup
     */
    private static Color[] cols = new Color[numCols];

    /**
     * The list of colour names. The mapping is performed by a direct index lookup.
     */
    private static String[] colNames = new String[numCols];

    static {
        colNames[0] = "white";
        cols[0] = new Color(255, 255, 255);

        colNames[1] = "dark grey";
        cols[1] = new Color(100, 100, 100);

        colNames[2] = "red";
        cols[2] = new Color(255, 0, 0);

        colNames[3] = "green";
        cols[3] = new Color(0, 255, 0);

        colNames[4] = "blue";
        cols[4] = new Color(0, 0, 255);

        colNames[5] = "cyan";
        cols[5] = new Color(0, 255, 255);

        colNames[6] = "magenta";
        cols[6] = new Color(255, 0, 255);

        colNames[7] = "yellow";
        cols[7] = new Color(224, 208, 0); // previously: new Color(245, 245, 0);

        colNames[8] = "pale green";
        cols[8] = new Color(152, 251, 152);

        colNames[9] = "light sky blue";
        cols[9] = new Color(135, 206, 250);

        colNames[10] = "orange";
        cols[10] = new Color(255, 165, 0);

        colNames[11] = "brown";
        cols[11] = new Color(200, 150, 100);

        colNames[12] = "pink";
        cols[12] = new Color(255, 200, 200);

        colNames[13] = "light grey";
        cols[13] = new Color(170, 170, 170);

        colNames[14] = "black";
        cols[14] = new Color(0, 0, 0);

        colNames[15] = "red 1";
        cols[15] = new Color(255, 63,  63);

        colNames[16] = "red 2";
        cols[16] = new Color(255, 127, 127);

        colNames[17] = "red 3";
        cols[17] = new Color(255, 191, 191);

        //Colours for Automatic Annotation Pipeline
        colNames[101] = "violet";
        cols[101] = new Color(102, 51, 153);

        colNames[102] = "purple";
        cols[102] = new Color(153, 102, 204);

        colNames[103] = "cream";
        cols[103] = new Color(255, 248, 220);
    }

    private static final int DEFAULT_INDEX = 5;

    /**
     * Return the <code>Color</code> corresponding to the given status number.
     * If the index is null, returns the default colour.
     *
     * @param i the index to look up
     * @return the corresponding colour
     */
    public static Color getColour(Integer i) {
        if (i == null) {
            return getDefaultColour();
        }
        Color ret = null;
        try {
            ret = cols[i];
        }
        catch (RuntimeException exp) {
            System.err.println("Unable to find colour for "+i);
            return getDefaultColour();
        }
        if (ret == null) {
            return getDefaultColour();
        }
        return ret;
    }

    /**
     * Return the <code>Color</code> used by default, if no explicit value is assigned
     *
     * @return the default colour
     */
    public static Color getDefaultColour() {
        return cols[DEFAULT_INDEX];
    }

    /**
     * Return the colour name corresponding to the given status number
     *
     * @param i the index to look up
     * @return the corresponding colour name
     */
    public static String getColourName(int i) {
        return colNames[i];
    }

    /**
     * Return the name of the colour used by default, if no explicit value is assigned
     *
     * @return the name of the default colour
     */
    public static String getDefaultColourName() {
        return colNames[DEFAULT_INDEX];
    }

    /**
     * Get the number of mappings
     *
     * @return the number of colour mappings
     */
    public static int getNumCols() {
        return numCols;
    }

    private static Map<String,Color> colorsByName;

    /**
     * Get the colour with the given name
     *
     * @param lookup The colour name (case-insensitive)
     * @return The corresponding <code>Color</code> object
     */
    public static Color getByName(String lookup) {
        if (colorsByName == null) {
            colorsByName = new HashMap<String,Color> ();
            for (int i = 0; i < numCols; i++)
                if (cols[i] != null)
                    colorsByName.put(colNames[i].toLowerCase(), cols[i]);
        }

        Color color = colorsByName.get(lookup.toLowerCase());
        if (color == null)
            return getDefaultColour();
        return color;
    }

    /**
     * Generate a 256-entry (i.e. 8-bit) colour model including
     * all the Artemis colours, with the remaining slots populated
     * with a range of shades of black mixed into the background
     * colour, to represent anti-aliased black text on the given
     * background.
     *
     * This can be used to generate transparent PNGs that use an
     * indexed colour model.
     *
     * @param backgroundColor the background colour onto which
     *  text will be drawn. If null, assume a transparent background.
     *  If a <code>backgroundColor</code> is supplied it is assumed to
     *  be opaque and its alpha value is ignored.
     * @return the colour model
     */
    public static IndexColorModel colorModel(Color backgroundColor) {
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        byte[] alphas = new byte[256];

        int numColors = 0;
        for (Color col: cols)
            if (col != null)
                numColors++;

        int i;
        /*
         * Note: we're assuming in both cases that an opaque black
         * is already available from the cols array.
         */
        if (backgroundColor == null) {
            for(i=0; i < 256 - numColors; i++) {
                reds[i] = greens[i] = blues[i] = 0;
                alphas[i] = (byte) ((i * 0xff) / (256 - numColors));
            }
        }
        else {
            reds[0] = greens[0] = blues[0] = alphas[0] = 0;

            int red = backgroundColor.getRed();
            int green = backgroundColor.getGreen();
            int blue = backgroundColor.getBlue();
            for(i=1; i < 256 - numColors; i++) {
                float ratio = (float) i / (255 - numColors);
                reds[i]   = (byte) (red * ratio);
                greens[i] = (byte) (green * ratio);
                blues[i]  = (byte) (blue * ratio);
                alphas[i] = (byte) 0xff;
            }
        }

        for (Color col: cols) {
            if (col == null)
                continue;

            reds[i]   = (byte) col.getRed();
            greens[i] = (byte) col.getGreen();
            blues[i]  = (byte) col.getBlue();
            alphas[i] = (byte) col.getAlpha();
            i++;
        }

        return new IndexColorModel(8, 256,
            reds, greens, blues, alphas);
    }


// colour_of_CDS = 5
// colour_of_cds? = 7
// colour_of_BLASTCDS = 2
// colour_of_BLASTN_HIT = 6
// colour_of_CRUNCH_D = 2
// colour_of_CRUNCH_X = 15
// colour_of_source = 0
// colour_of_prim_tran = 0
// colour_of_stem_loop = 2
// colour_of_misc_feature = 3
// colour_of_misc_RNA = 12
// colour_of_delta = 3
// colour_of_LTR = 4
// colour_of_repeat_region = 9
// colour_of_repeat_unit = 9
// colour_of_terminator = 3
// colour_of_promoter = 3
// colour_of_intron = 1
// colour_of_exon = 7
// colour_of_mRNA = 1
// colour_of_tRNA = 8
// colour_of_TATA = 3
// colour_of_bldA = 2
// colour_of_GFF = 11

// colour_of_start_codon = 6

}

