package org.genedb.web.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.sf.json.JSONString;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.objects.LocatedFeature;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;
import org.genedb.web.gui.TrackedDiagram.AllocatedCompoundFeature;

/**
 * Renders a {@link ContextMapDiagram} as an image.
 *
 * The rendered diagram consists of a number of gene tracks (positive above and
 * negative below). In the centre is a scale track, which shows the scale of the
 * diagram.
 *
 * Each exon is represented by an unbordered rectangle of the appropriate
 * colour, centred vertically within the gene track. An intron is represented by
 * a narrower rectangle of the same colour, also vertically centred and
 * continuous with the exons it separates.
 *
 * If the rendered diagram is more than 32767 pixels wide, the image will still
 * be correctly generated but most decoding software will fail in one of several
 * amusing ways. Therefore it is advisable to use a ContextMapDiagram
 * representing fewer than 327670 bases (assuming the current default scale of
 * 10 bases per pixel). Moreover, a rendered diagram even 10k pixels wide
 * appears to crash at least some versions of Firefox on Linux, so we are
 * currently limiting ourselves to tiles 5000 pixels wide.
 *
 * @author rh11
 */
public class RenderedContextMap {
    private static final Logger logger = Logger.getLogger(RenderedContextMap.class);

    private String filenamePrefix = "";

    private static final String FILE_FORMAT = "png";
    private static final String FILE_EXT = "png";

    /*
     * A note on the color model choice: the choice of color model will
     * control which type of PNG file is generated. Typically the indexed
     * color model results in smaller files for full context map images,
     * and the reverse is true for small chromosome thumbnails.
     *
     * Unfortunately a bug in the AWT rendering engine (#6712736) means
     * that anti-aliased text cannot be drawn directly on a transparent
     * background to an image with an indexed color model. Therefore
     * in that case we draw the text to an image with direct color model
     * and copy the result to the indexed image. In fact we can get
     * away with simply blitting it over a rectangle of the indexed image,
     * since the containing rectangle should not overlap anything else.
     * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6712736
     *
     * Thus we need a direct-color image on which to write the labels. Rather
     * than create a new one each time, we use the same one for all the
     * labels. It needs to be large enough to contain the label text,
     * as specified by the constants MAX_LABEL_WIDTH and MAX_LABEL_HEIGHT.
     * Should the need arise for labels of wildly varying sizes this
     * can be reconsidered, but it will suffice for now.
     */

    private enum ColorModel { DIRECT, INDEXED };

    /*
     * These are used for the label buffer, when the INDEXED color model is in use.
     */
    private static final int MAX_LABEL_WIDTH  = 100;
    private static final int MAX_LABEL_HEIGHT = 50;

    /**
     * What color model should be used?
     */
    private ColorModel colorModel = ColorModel.INDEXED;

    /**
     * The scale at which this diagram is drawn, in bases per pixel.
     */
    private int basesPerPixel = 10;

    /**
     * The height of a gene track, in pixels.
     */
    private int geneTrackHeight = 20;
    /**
     * The height of the rectangle representing an exon
     */
    private int exonRectHeight = 12;
    /**
     * The height of the rectangle representing an intron
     */
    private int intronRectHeight = 2;

    /**
     * The height of the rectangle representing an untranslated region
     */
    private int utrRectHeight = 6;

//    /**
//     * The width in pixels of the border drawn around a UTR
//     */
//    private float utrStrokeSize = 3.0f;
//    private Stroke utrStroke = new BasicStroke(utrStrokeSize);

//    private int utrGapHeight = 4;

    private int axisBreakX = 2;
    private int axisBreakY = 4;
    private int axisBreakGap = 6;

    /**
     * The height of the scale track.
     */
    private int scaleTrackHeight = 22;

    /**
     * The colour of the scale.
     */
    private Color scaleColor = Color.GRAY;

    /**
     * The colour of the labels.
     */
    private Color labelColor = Color.BLACK;

    /**
     * The colour of the label background. If <code>null</code>, no label background is printed.
     * (Note that LCD text antialiasing doesn't work on a transparent background. Also note
     * that IE6 does not ordinarily render partial transparency, though there is a workaround
     * using AlphaImageLoader.)
     */
    private Color labelBackgroundColor = new Color(0xF0, 0xF0, 0xE4);

    /**
     * The anti-aliasing mode used to draw label text.
     */
    private Object labelAntialiasingMode = RenderingHints.VALUE_TEXT_ANTIALIAS_ON; //LCD_HRGB;

    /**
     * Font used for printing figures on the scale track
     */
    private Font labelFont = new Font("FuturaTMed", Font.PLAIN, 12);

    /**
     * Distance between minor scale ticks, in bases
     */
    private int minorTickDistance = 200;

    /**
     * Distance between major scale ticks, in bases.
     * Must be a multiple of <code>MINOR_TICK_DISTANCE</code>.
     */
    private int majorTickDistance = 1000;

    /**
     * Height of each minor scale tick, in pixels
     */
    private static final int MINOR_TICK_HEIGHT = 4;

    /**
     * Height of each minor scale tick, in pixels
     */
    private static final int MAJOR_TICK_HEIGHT = 8;

    /**
     * Vertical position of scale axis within scale track,
     * measured in pixels downwards from the top of the scale track.
     */
    private int scaleVerticalPos = MAJOR_TICK_HEIGHT / 2;

    /**
     * How much space to leave between a major scale tick and the label below, in pixels.
     */
    private static final int LABEL_SEP = 2;


    private ContextMapDiagram diagram;
    private NavigableMap<Integer,Gap> gapsByStart = new TreeMap<Integer,Gap>();

    private int start, end;
    private int sizeExcludingGaps;

    private int numberOfPositiveTracks, numberOfNegativeTracks;

    public RenderedContextMap(ContextMapDiagram diagram) {

        this.diagram = diagram;
        this.start = diagram.getStart();
        this.end = diagram.getEnd();

        for (LocatedFeature globalFeature: diagram.getGlobalFeatures()) {
            if (globalFeature instanceof Gap) {
                Gap gap = (Gap) globalFeature;
                gapsByStart.put(gap.getFmin(), gap);
            }
        }
        calculateSizeExcludingGaps();

        this.numberOfPositiveTracks = diagram.numberOfPositiveTracks();
        this.numberOfNegativeTracks = diagram.numberOfNegativeTracks();
    }

    /**
     * Compute the size, excluding gaps, between
     * <code>start</code> and <code>end</code>,
     * and store the result in <code>sizeExcludingGaps</code>.
     */
    private void calculateSizeExcludingGaps() {
        sizeExcludingGaps = 0;
        int loc = getStart();
        for (Gap gap : gapsByStart.values()) {
            if (loc < gap.getFmin()) {
                sizeExcludingGaps += gap.getFmin() - loc;
                loc = gap.getFmax();
            }
        }

        if (loc < getEnd())
            sizeExcludingGaps += getEnd() - loc;
    }

    public RenderedContextMap restrict(int start, int end) {
        if (start < diagram.getStart()) {
            logger.warn(String.format("Start of diagram is %d, start of restriction is %d",
                diagram.getStart(), start));
            this.start = diagram.getStart();
        }
        else
            this.start = start;

        if (end > diagram.getEnd()) {
            logger.warn(String.format("End of diagram is %d, end of restriction is %d",
                diagram.getEnd(), end));
            this.end = diagram.getEnd();
        }
        else
            this.end = end;

        return this;
    }

    /**
     * Get the location of the start of the diagram.
     *
     * @return the start of the diagram, in interbase coordinates
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the location of the end of the diagram.
     *
     * @return the end of the diagram, in interbase coordinates
     */
    public int getEnd() {
        return end;
    }

    public String getPreferredFilename () {
        return String.format("%s%09d-%09ds%d.%s", filenamePrefix, getStart(), getEnd(),
            getBasesPerPixel(), FILE_EXT);
    }

    private String getPreferredFilenameForTile (int tileIndex, int tileWidth) {
        return String.format("%s%09d-%09ds%dt%dw%d.%s", filenamePrefix, getStart(), getEnd(),
            getBasesPerPixel(), tileIndex, tileWidth, FILE_EXT);
    }

    public ContextMapDiagram getDiagram() {
        return this.diagram;
    }

    /**
     * Configure this diagram to render as a thumbnail.
     *
     * @param maxWidth the maximum width, in pixels, of the rendered thumbnail
     * @return this object
     */
    public RenderedContextMap asThumbnail(int maxWidth) {
        setMaxWidth(maxWidth, 0);
        setScaleTrackHeight(1);
        setGeneTrackHeight(2);
        setExonRectHeight(2);
        setIntronRectHeight(2);
        setUTRRectHeight(2);
        setTickDistances(0, 0);
        setAxisBreakSlash(0, 0);
        setScaleColor(Color.GRAY);
        filenamePrefix = "thumb-";
        forceTracks(2, 2);

         /* For thumbnails, the resulting file is usually smaller with a direct color model.
          * This means we have to apply the AlphaImageLoader hack to the chromosome thumbnail
          * for IE6. */
        this.colorModel = ColorModel.DIRECT;

        return this;
    }

    /**
     * Get the width in pixels
     * @return the width in pixels of this rendered diagram
     */
    public int getWidth() {
        return pixelWidth(getStart(), getEnd());
    }

    /**
     * Constrain the diagram to fit within a fixed width,
     * for a given gap size, by adjusting the scale. The
     * resulting width will be as close as possible to maxWidth,
     * but no larger. The <code>axisBreakGap</code> is also
     * set equal to the supplied value.
     *
     * @param maxWidth the maximum allowed width, in pixels
     * @param axisBreakGap the size, in pixels, of a break in the axis
     *          representing a gap.
     * @return the actual width of the diagram
     */
    public int setMaxWidth(int maxWidth, int axisBreakGap) {
        this.axisBreakGap = axisBreakGap;

        int maxWidthExcludingGaps = maxWidth - axisBreakGap * this.gapsByStart.size();
        if (sizeExcludingGaps % maxWidthExcludingGaps == 0)
            setBasesPerPixel(sizeExcludingGaps / maxWidthExcludingGaps);
        else
            setBasesPerPixel((sizeExcludingGaps / maxWidthExcludingGaps) + 1);

        calculateContigs();
        assert getWidth() <= maxWidth;
        return getWidth();
    }

    /**
     * Get the height in pixels
     * @return the height in pixels of this rendered diagram
     */
    public int getHeight() {
        return scaleTrackHeight + numberOfTracks() * geneTrackHeight;
    }

    /**
     * How many tracks will this diagram have, when rendered?
     * May be different from getDiagram().numberOfTracks(),
     * if {@link #forceTracks(int,int)} has been used.
     *
     * @return
     */
    private int numberOfTracks() {
        return numberOfPositiveTracks + numberOfNegativeTracks;
    }

    /**
     * Force the rendered diagram to have the specified number of tracks.
     * Thus there may be empty tracks in the rendered diagram,
     * and higher-numbered tracks will not be shown.
     *
     * @param positiveTracks
     * @param negativeTracks
     */
    private void forceTracks(int positiveTracks, int negativeTracks) {
        this.numberOfPositiveTracks = positiveTracks;
        this.numberOfNegativeTracks = negativeTracks;
    }

    /**
     * Get the height of the gene tracks of this diagram.
     * @return the height in pixels
     */
    public int getGeneTrackHeight() {
        return geneTrackHeight;
    }

    /**
     * Set the height of the gene tracks of this diagram.
     * @param geneTrackHeight the height in pixels
     */
    public void setGeneTrackHeight(int geneTrackHeight) {
        this.geneTrackHeight = geneTrackHeight;
    }

    /**
     * Get the height of the rectangles used to represent exons in this diagram.
     * @return the height in pixels
     */
    public int getExonRectHeight() {
        return exonRectHeight;
    }

    /**
     * Set the height of the rectangles used to represent exons in this diagram
     * @param exonRectHeight the height in pixels
     */
    public void setExonRectHeight(int exonRectHeight) {
        this.exonRectHeight = exonRectHeight;
    }

    /**
     * Get the height of the rectangles used to represent introns in this diagram.
     * @return the height in pixels
     */
   public int getIntronRectHeight() {
        return intronRectHeight;
    }

   /**
    * Set the height of the rectangles used to represent introns in this diagram
    * @param intronRectHeight the height in pixels
    */
    public void setIntronRectHeight(int intronRectHeight) {
        this.intronRectHeight = intronRectHeight;
    }

    /**
     * Get the height of the rectangles used to represent untranslated regions in this diagram.
     * @return the height in pixels
     */
    public int getUTRRectHeight() {
        return utrRectHeight;
    }

    /**
     * Set the height of the rectangles used to represent untranslated regions in this diagram
     * @param utrRectHeight the height in pixels
     */
    public void setUTRRectHeight(int utrRectHeight) {
        this.utrRectHeight = utrRectHeight;
    }

    /**
     * Get the height of the scale track of this diagram.
     * @return the height in pixels
     */
    public int getScaleTrackHeight() {
        return scaleTrackHeight;
    }

    /**
     * Set the height of the scale track.
     * @param scaleTrackHeight the height in pixels
     */
    public void setScaleTrackHeight(int scaleTrackHeight) {
        this.scaleTrackHeight = scaleTrackHeight;
    }

    /**
     * Get the colour of the scale.
     * @return the colour of the scale
     */
    public Color getScaleColor() {
        return scaleColor;
    }

    /**
     * Set the colour of the scale.
     * @param scaleColor the colour of the scale
     */
    public void setScaleColor(Color scaleColor) {
        this.scaleColor = scaleColor;
    }

    /**
     * Get the colour of the labels on the scale track.
     * @return the colour of the labels
     */
    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Set the colour of the labels on the scale track.
     * @param labelColor the colour of the labels
     */
    public void setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
    }

    /**
     * Get the distance between minor (unlabelled) ticks on the
     * scale track of this diagram.
     * @return the distance in bases
     */
    public int getMinorTickDistance() {
        return minorTickDistance;
    }

    /**
     * Set the distance between major (labelled) and minor (unlabelled) ticks on
     * the scale track of this diagram. The <code>majorTickDistance</code>
     * must be a multiple of the <code>minorTickDistance</code>. If the major
     * tick distance is zero, then only minor ticks will be drawn. If the tick
     * distances are both zero, then no scale ticks will be drawn.
     *
     * @param majorTickDistance the distance in bases
     * @param minorTickDistance the distance in bases
     */
    public void setTickDistances(int majorTickDistance, int minorTickDistance) {
        if (minorTickDistance != 0 && majorTickDistance % minorTickDistance != 0)
            throw new IllegalArgumentException(String.format(
                "Major tick distance %d is not a multiple of minor tick distance %d",
                majorTickDistance, minorTickDistance));

        this.majorTickDistance = majorTickDistance;
        this.minorTickDistance = minorTickDistance;

        if (majorTickDistance == 0)
            scaleVerticalPos = 0;
    }

    protected void setAxisBreakSlash(int axisBreakX, int axisBreakY) {
        this.axisBreakX = axisBreakX;
        this.axisBreakY = axisBreakY;
    }

    protected void setAxisBreakGap(int axisBreakGap) {
        this.axisBreakGap = axisBreakGap;
    }

    /**
     * Get the distance between major (labelled) ticks on the scale
     * track of this diagram. Always a multiple of the minor tick distance.
     * @return
     */
    public int getMajorTickDistance() {
        return majorTickDistance;
    }

    public String getRelativeRenderDirectory() {
        return String.format("%s/%s", getDiagram().getOrganism(), getDiagram().getChromosome());
    }

    public List<RenderedContextMap.Tile> renderTilesTo(String directory, int tileWidth) {
        String absoluteRenderDirectory = directory + "/" + getRelativeRenderDirectory();
        logger.debug(String.format("Rendering tiles to '%s'", absoluteRenderDirectory));

        File dir = new File(absoluteRenderDirectory);
        dir.mkdirs();
        if (!dir.isDirectory())
            throw new RuntimeException(String.format("Failed to create directory '%s'", dir));

        beforeRender();
        drawScaleTrack();
        renderFeatures();

        List<RenderedContextMap.Tile> tiles = new ArrayList<RenderedContextMap.Tile>();

        if (getWidth() != image.getWidth())
            throw new IllegalStateException(String.format(
                "Image width: expected=%d, actual=%d", getWidth(), image.getWidth()));

        for (int startOfTile = 0, tileIndex = 1; startOfTile < getWidth(); startOfTile += tileWidth, tileIndex++) {
            String preferredFilename = getPreferredFilenameForTile(tileIndex, tileWidth);
            File tileFile = new File(dir, preferredFilename);

            int widthOfThisTile = tileWidth;
            if (startOfTile + widthOfThisTile > getWidth())
                widthOfThisTile = getWidth() - startOfTile;

            if (!tileFile.exists()) {
                File tileTempFile;
                try {
                    tileTempFile = File.createTempFile(preferredFilename, null, dir);
                }
                catch (IOException e) {
                    throw new RuntimeException(String.format(
                        "Failed to create temp file for tile %d in directory '%s'", tileIndex, dir), e);
                }

                logger.debug(String.format("Rendering tile %d (start=%d,width=%d) to '%s'", tileIndex, startOfTile, widthOfThisTile, tileTempFile));
                BufferedImage tile = image.getSubimage(startOfTile, 0, widthOfThisTile, image.getHeight());
                try {
                    ImageIO.write(tile, FILE_FORMAT, new FileOutputStream(tileTempFile));
                } catch (IOException e) {
                    tileTempFile.delete();
                    throw new RuntimeException(String.format("Failed to write image file '%s'", tileTempFile), e);
                }

                if (!tileTempFile.renameTo(tileFile))
                    throw new RuntimeException(String.format("Failed to rename '%s' to '%s'", tileTempFile, tileFile));
            }

            tiles.add(new Tile(widthOfThisTile, preferredFilename));
        }

        return tiles;
    }

    private Graphics2D graf, labelGraf;
    private BufferedImage image, labelBuffer;
    private List<RenderedFeature> renderedFeatures;

    /**
     * A navigable map from base -> pixel, with an entry corresponding to
     * the beginning of each contiguous region. It is populated at the start
     * of rendering, by {@link #drawScaleLine()}, and used for the duration
     * of the rendering process.
     */
    private NavigableMap<Integer,Double> contigs;

    private void calculateContigs() {
        contigs = new TreeMap<Integer,Double>();
        double x = xCoordinate(getStart());
        for (Gap gap: gapsByStart.values()) {
            x = xCoordinateAsDouble(gap.getFmin()) + axisBreakGap;
            contigs.put(gap.getFmax(), x);
        }
    }

    private void beforeRender() {
        calculateContigs();
        switch (colorModel) {
        case DIRECT:
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            labelBuffer = null;
            break;
        case INDEXED:
            image = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_BYTE_INDEXED, ArtemisColours.colorModel(labelBackgroundColor));
            labelBuffer = new BufferedImage(MAX_LABEL_WIDTH, MAX_LABEL_HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
            break;
        }
        graf = (Graphics2D) image.getGraphics();
        if (labelBuffer != null) {
            labelGraf = (Graphics2D) labelBuffer.getGraphics();
            labelGraf.setComposite(AlphaComposite.Src);
            labelGraf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, labelAntialiasingMode);
            labelGraf.setFont(labelFont);
        }

        renderedFeatures = new ArrayList<RenderedFeature>();
    }

    private void afterRender() {
        if (labelGraf != null)
            labelGraf.dispose();

        image = null;
        graf.dispose();
        graf = null;
    }

    public void writeTo(OutputStream out) throws IOException {

        logger.debug(String.format("Drawing RenderedContextMap with dimensions %dx%d", getWidth(), getHeight()));

        beforeRender();
        drawScaleTrack();
        renderFeatures();

        ImageIO.write(image, FILE_FORMAT, out);

        afterRender();
    }

    private void renderFeatures() {
        SortedSet<AllocatedCompoundFeature> features = diagram.getAllocatedCompoundFeatures();
        for (AllocatedCompoundFeature superfeature : features) {
            int track = superfeature.getTrack();
            CompoundLocatedFeature compoundFeature = superfeature.getFeature();
            List<? extends LocatedFeature> subfeatures = compoundFeature.getSubfeatures();
            if (subfeatures.size() == 0) {
                logger.error(String.format("Gene '%s' has no transcripts!", compoundFeature.getUniqueName()));
                continue;
            }
            if (compoundFeature.getFmax() < this.getStart() || compoundFeature.getFmin() > this.getEnd())
                continue;

            for (LocatedFeature subfeature: subfeatures) {
                if (!(subfeature instanceof Transcript)) {
                    logger.error(String.format("Located feature '%s' is a '%s' not a Transcript", superfeature, superfeature.getClass()));
                    continue;
                }

                renderedFeatures.add(new RenderedFeature(compoundFeature, subfeature, track, ((Transcript) subfeature).getProducts()));

                drawTranscript(track, (Transcript) subfeature);

                track += Integer.signum(track);
            }
        }
    }

    /**
     * Once the diagram has been rendered, this method will return a list of
     * all the rendered features with their locations on the rendered image.
     *
     * @return
     */
    public List<RenderedFeature> getRenderedFeatures() {
        return renderedFeatures;
    }

    /**
     * Get the scale at which this diagram is drawn, in bases per pixel.
     *
     * @return
     */
    public int getBasesPerPixel() {
        return basesPerPixel;
    }

    /**
     * Set the scale at which this diagram should be drawn. Will adjust the
     * width appropriately.
     *
     * @param basesPerPixel the new scale, in bases per pixel
     */
    public void setBasesPerPixel(int basesPerPixel) {
        if (basesPerPixel <= 0)
            throw new IllegalArgumentException(String.format("Cannot have %d bases per pixel!",
                basesPerPixel));

        this.basesPerPixel = basesPerPixel;
    }



    private int yCoordinateOfAxis() {
        return topOfTrack(0) + scaleVerticalPos;
    }

    /**
     * Draw the scale line, marking any gaps. Does not draw ticks.
     * Also adds the gaps to <code>renderedFeatures</code>.
     */
    private void drawScaleLine() {
        graf.setColor(scaleColor);
        int x = xCoordinate(getStart()), y = yCoordinateOfAxis();
        for (Gap gap: gapsByStart.values()) {
            renderedFeatures.add(new RenderedFeature(
                String.format("gap: %d-%d", gap.getFmin()+1, gap.getFmax()),
                /*track*/ 0, xCoordinate(gap.getFmin()), axisBreakGap));
            int gapStartX = xCoordinate(gap.getFmin());

            if (labelBackgroundColor != null && axisBreakGap > 0) {
                graf.setColor(labelBackgroundColor);
                graf.fillRect(gapStartX - axisBreakX - 1, y - axisBreakY - 1, 2 * axisBreakX + axisBreakGap + 3, 2 * axisBreakY + 3);
                graf.setColor(scaleColor);
            }

            graf.drawLine(x, y, gapStartX, y);

            if (axisBreakGap > 0) {
                graf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graf.drawLine(gapStartX + axisBreakX, y - axisBreakY, gapStartX - axisBreakX, y + axisBreakY);
                graf.drawLine(gapStartX + axisBreakX + axisBreakGap, y - axisBreakY, gapStartX - axisBreakX + axisBreakGap, y + axisBreakY);
                graf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            x = xCoordinate(gap.getFmin()) + axisBreakGap;
        }
        graf.drawLine(x, y, xCoordinate(getEnd()), y);
    }

    private void drawScaleTrack() {

        drawScaleLine();

        if (minorTickDistance == 0)
            return;

        int majorTicksEvery = (majorTickDistance == 0 ? Integer.MAX_VALUE : (majorTickDistance / minorTickDistance));
        int tickNumber = 0;
        int pos = majorTickDistance * (getStart() / majorTickDistance);
        while (pos <= getEnd()) {
            if (tickNumber++ % majorTicksEvery == 0)
                drawMajorScaleTick(pos);
            else
                drawMinorScaleTick(pos);
            pos += minorTickDistance;
        }
    }

    private void drawMinorScaleTick(int pos) {
        drawScaleTick(pos, MINOR_TICK_HEIGHT);
    }

    private void drawMajorScaleTick(int pos) {
        if (drawScaleTick(pos, MAJOR_TICK_HEIGHT))
            drawLabel(pos);
    }

    private void drawLabel(int pos) {
        switch (colorModel) {
        case DIRECT:
            drawLabelDirectly(pos);
            break;
        case INDEXED:
            drawLabelIndirectly(pos);
        }
    }

    private void drawLabelDirectly(int pos) {
        graf.setFont(labelFont);
        Color previousColor = graf.getColor();
        graf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, labelAntialiasingMode);

        FontRenderContext fontRenderContext = graf.getFontRenderContext();
        Font font = graf.getFont();
        String labelString = String.valueOf(pos);
        int labelHalfWidth = (int) font.getStringBounds(labelString, fontRenderContext).getCenterX();
        LineMetrics labelMetrics = font.getLineMetrics(labelString, fontRenderContext);

        int x = xCoordinate(pos) - labelHalfWidth;
        int y = yCoordinateOfAxis() + (MAJOR_TICK_HEIGHT / 2) + LABEL_SEP + (int) labelMetrics.getAscent();
        if (labelBackgroundColor != null) {
            graf.setColor(labelBackgroundColor);
            graf.fillRect(x, yCoordinateOfAxis() + (MAJOR_TICK_HEIGHT / 2) + LABEL_SEP, labelHalfWidth * 2, (int) labelMetrics.getHeight());
        }

        graf.setColor(labelColor);
        graf.drawString(labelString, x, y);

        graf.setColor(previousColor);
    }

    /*
     * Thanks to Java bug #6712736: if we want antialiased text on an indexed
     * image, we must draw it to a direct image first and copy the result.
     */
    private static final Color TRANSPARENT = new Color(0,0,0,0);
    private void drawLabelIndirectly(int pos) {
        FontRenderContext fontRenderContext = labelGraf.getFontRenderContext();
        Font font = labelGraf.getFont();
        String labelString = String.valueOf(pos);

        /*
         * Note: getStringBounds() is not generally guaranteed to give a rectangle
         * that visually encloses all the rendered text, but in this case (printing
         * numerals) it is reasonable to suppose that it will.
         */
        Rectangle2D labelBounds = font.getStringBounds(labelString, fontRenderContext);
        LineMetrics labelMetrics = font.getLineMetrics(labelString, fontRenderContext);

        int w = (int) labelBounds.getWidth();
        int h = (int) labelBounds.getHeight();
        if (labelBackgroundColor == null)
            labelGraf.setColor(TRANSPARENT);
        else
            labelGraf.setColor(labelBackgroundColor);
        labelGraf.fillRect(0, 0, w, h);

        labelGraf.setColor(labelColor);
        labelGraf.drawString(labelString, 0, labelMetrics.getAscent());

        int[] labelData = labelBuffer.getRGB(0, 0, w, h, null, 0, w);
        int x = xCoordinate(pos) - (int) labelBounds.getCenterX();
        int y = yCoordinateOfAxis() + (MAJOR_TICK_HEIGHT / 2) + LABEL_SEP;

        /*
         * Deal with the case where the label only partially
         * intersects the image, at the left or right edge
         * of a tile.
         */
        int offset = 0, destWidth = w;
        if (x < 0) {
            offset = -x;
            destWidth += x;
            x = 0;
        }
        else if (x+w > getWidth()) {
            destWidth = getWidth() - x;
            x = getWidth() - destWidth;
        }
        if (y + h > image.getHeight())
            h = image.getHeight() - y;
        image.setRGB(x, y, destWidth, h, labelData, offset, w);
    }

    /**
     * Draw a scale tick.
     * @param pos the position of the tick, in interbase coordinates.
     * @param tickHeight the height of the tick, in pixels
     * @return a boolean value indicating whether the tick has been drawn.
     *          If the position falls into a gap, the tick will not be drawn
     *          and this method will return <code>false</code>.
     */
    private boolean drawScaleTick(int pos, int tickHeight) {

        if (gapContainingLocation(pos) != null)
            return false;

        int topOfTick    = yCoordinateOfAxis() - (tickHeight / 2);
        int bottomOfTick = yCoordinateOfAxis() + (tickHeight / 2);

        int x = xCoordinate(pos);
        graf.drawLine(x, topOfTick, x, bottomOfTick);
        return true;
    }

    private void drawTranscript(int trackNumber, Transcript transcript) {
        logger.debug(String.format("Drawing transcript %s (%d..%d) on track %d", transcript
                .getUniqueName(), transcript.getFmin(), transcript.getFmax(), trackNumber));

        Color color = ArtemisColours.getColour(transcript.getColourId());
        graf.setColor(color);
        //graf.setStroke(utrStroke);

        int currentPos = transcript.getFmin();
        for (TranscriptComponent component : transcript.getComponents()) {
            drawIntron(trackNumber, currentPos, component.getStart());
            drawComponent(trackNumber, component);
            currentPos = component.getEnd();
        }
        drawIntron(trackNumber, currentPos, transcript.getFmax());
    }

    private void drawIntron(int trackNumber, int start, int end) {
        if (end <= start) {
            return;
        }
        int x = xCoordinate(start), y = topOfIntron(trackNumber);
        int width = pixelWidth(start, end);
        graf.fillRect(x, y, width, intronRectHeight);
    }

    private void drawComponent(int trackNumber, TranscriptComponent component) {
        int start = component.getStart(), end = component.getEnd();
        if (end <= start) {
            logger.warn("Drawing nothing: empty or negative");
            return;
        }

        int x = xCoordinate(component.getStart());
        int width = pixelWidth(component);

        if (component instanceof Exon)
            drawExon(trackNumber, x, width);
        else
            drawUTR(trackNumber, x, width);
    }

    private void drawExon(int trackNumber, int x, int width) {
        /*
         * Always draw exons at least 1 pixel wide, otherwise it
         * looks confusing. See the P. falciparum gene PF14_0177
         * for example.
         */
        if (width < 1)
            width = 1;

        int y = topOfExon(trackNumber);
        graf.fillRect(x, y, width, exonRectHeight);
    }

    private void drawUTR(int trackNumber, int x, int width) {
//        graf.draw(new Rectangle2D.Float(
//            x + utrStrokeSize/2, y + utrStrokeSize/2,
//            width - utrStrokeSize, exonRectHeight - utrStrokeSize));

//        int y = topOfExon(trackNumber);
//        int height = (exonRectHeight - utrGapHeight) / 2;
//        graf.fillRect(x, y, width, height);
//        graf.fillRect(x, y + height + utrGapHeight, width, height);

        int y = topOfUTR(trackNumber);
        graf.fillRect(x, y, width, utrRectHeight);
   }

    /**
     * Calculate the x-position (in pixels relative to this diagram)
     * corresponding to a particular chromosome location.
     *
     * @param loc the chromosome location
     * @return the corresponding x position
     */
    private int xCoordinate(int loc) {
        return (int) Math.round(xCoordinateAsDouble(loc));
    }
    private double xCoordinateAsDouble(int loc) {
        Gap gapContainingLocation = gapContainingLocation(loc);
        if (gapContainingLocation != null) {
            logger.warn(String.format("Diagram location %d lies in the gap '%s'", loc, gapContainingLocation.getUniqueName()));
            Double gapEndPx = contigs.get(gapContainingLocation.getFmax());
            if (gapEndPx == null)
                throw new IllegalStateException("Failed to locate gap. This should never happen!");
            return gapEndPx - axisBreakGap / 2;
        }
        Map.Entry<Integer, Double> e = contigs.floorEntry(loc);
        if (e == null)
            return naivePixelWidth(getStart(), loc);

        return e.getValue() + naivePixelWidth(e.getKey(), loc);
    }

    /**
     * Calculate the width in pixels of a transcript component.
     *
     * @param transcriptComponent the transcript component
     * @return the width in pixels
     */
    private int pixelWidth(TranscriptComponent transcriptComponent) {
        return pixelWidth(transcriptComponent.getStart(), transcriptComponent.getEnd());
    }

    /**
     * Calculate the width in pixels of a feature of the diagram.
     *
     * @param locatedFeature the feature in question
     * @return the width in pixels
     */
    private int pixelWidth(LocatedFeature locatedFeature) {
        return pixelWidth(locatedFeature.getFmin(), locatedFeature.getFmax());
    }

    /**
     * Calculate the width in pixels of a segment of the diagram.
     *
     * @param start the start location, in interbase coordinates
     * @param end the end location, in interbase coordinates
     * @return the width in pixels
     */
    private int pixelWidth(int start, int end) {
        return xCoordinate(end) - xCoordinate(start);
    }

    private double naivePixelWidth(int start, int end) {
        double startPx = (double) start / basesPerPixel;
        double endPx   = (double) end / basesPerPixel;

        return (endPx - startPx);
    }

    private Gap gapContainingLocation(int loc) {
        Map.Entry<Integer, Gap> e = gapsByStart.lowerEntry(loc);
        if (e == null || loc >= e.getValue().getFmax())
            return null;
        return e.getValue();
    }

    /**
     * Calculate the y-coordinate of the top of a track.
     *
     * @param trackNumber
     * @return
     */
    private int topOfTrack(int trackNumber) {
        int firstGuess = (numberOfPositiveTracks - trackNumber) * geneTrackHeight;

        // The first guess is right for non-negative tracks.
        // Also, it's always right as long as the scale track is the same height
        // as the gene tracks.
        if (scaleTrackHeight == geneTrackHeight || trackNumber >= 0)
            return firstGuess;

        // Otherwise, correct for the differing height of the scale track.
        return firstGuess - geneTrackHeight + scaleTrackHeight;
    }

    private int topOfIntron(int trackNumber) {
        return topOfTrack(trackNumber) + (geneTrackHeight - intronRectHeight) / 2;
    }

    private int topOfExon(int trackNumber) {
        return topOfTrack(trackNumber) + (geneTrackHeight - exonRectHeight) / 2;
    }

    private int topOfUTR(int trackNumber) {
        return topOfTrack(trackNumber) + (geneTrackHeight - utrRectHeight) / 2;
    }

    public class Tile implements JSONString {
        private int width;
        private String filename;

        public Tile(int width, String filename) {
            this.width = width;
            this.filename = filename;
        }

        public String toJSONString() {
            return String.format("[%d,\"%s\"]", width, filename);
        }
    }

    private List<String> products = new ArrayList<String>();
    private Map<String,Integer> productIndexByName = new HashMap<String,Integer>();
    private int productIndex(String productName) {
        if (productIndexByName.containsKey(productName))
            return productIndexByName.get(productName);
        else {
            products.add(productName);
            int indexOfProduct = products.size() - 1;
            productIndexByName.put(productName, indexOfProduct);
            return indexOfProduct;
        }
    }

    public List<String> getProducts() {
        return products;
    }

    /**
     * Represents a located feature that has been rendered,
     * and stores its position on the rendered diagram.
     *
     * @author rh11
     */
    public class RenderedFeature implements JSONString {
        private String uniqueName, compoundUniqueName = "", compoundName = "";
        private int track, pixelX, pixelWidth;
        private List<Integer> productIndices = new ArrayList<Integer>();
        public RenderedFeature (CompoundLocatedFeature compoundFeature, LocatedFeature subfeature, int track, List<String> productNames) {
            this(subfeature.getUniqueName(), compoundFeature.getUniqueName(),
            compoundFeature.getName(), track,
            xCoordinate(subfeature.getFmin()), pixelWidth(subfeature));

            if (productNames != null) {
                for (String productName: productNames)
                    productIndices.add(productIndex(productName));
            }
        }
        public RenderedFeature(String uniqueName, int track, int pixelX, int pixelWidth) {
            this.uniqueName = uniqueName;
            this.track = track;
            this.pixelX = pixelX;
            this.pixelWidth = pixelWidth;
        }
        private RenderedFeature(String uniqueName, String compoundUniqueName, String compoundName, int track, int pixelX, int pixelWidth) {
            this(uniqueName, track, pixelX, pixelWidth);

            if (compoundUniqueName != null)
                this.compoundUniqueName = compoundUniqueName;

            if (compoundName != null)
                this.compoundName = compoundName;
        }
        private String getProductIndicesAsJSONString() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int productIndex: productIndices) {
                if (first)
                    first = false;
                else
                    sb.append(',');
                sb.append(productIndex);
            }
            return "[" + sb.toString() + "]";
        }
        public String toJSONString() {
            return String.format("[\"%s\",\"%s\",\"%s\",%d,%d,%d,%s]",
                uniqueName, compoundUniqueName, compoundName, track, pixelX, pixelWidth, getProductIndicesAsJSONString());
        }
    }
}
