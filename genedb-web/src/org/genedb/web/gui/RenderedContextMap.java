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
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Transcript;

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
 * 10 bases per pixel).
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
    private int exonRectHeght = 12;
    /**
     * The height of the rectangle representing an intron
     */
    private int intronRectHeight = 2;

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
    private static final int SCALE_VERTICAL_POS = MAJOR_TICK_HEIGHT / 2;

    /**
     * How much space to leave between a major scale tick and the label below, in pixels.
     */
    private static final int LABEL_SEP = 2;


    private ContextMapDiagram diagram;
    private int start, end;
    private int width;

    private int numberOfPositiveTracks, numberOfNegativeTracks;

    private BufferedImage image, labelBuffer;
    private Graphics2D graf, labelGraf;

    public RenderedContextMap(ContextMapDiagram diagram) {
        this.diagram = diagram;
        this.width = diagram.getSize() / basesPerPixel;
        this.start = diagram.getStart();
        this.end = diagram.getEnd();
        this.numberOfPositiveTracks = diagram.numberOfPositiveTracks();
        this.numberOfNegativeTracks = diagram.numberOfNegativeTracks();
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

        this.width = getSize() / basesPerPixel;
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

    /**
     * Get the size of the diagram.
     *
     * @return the size of the diagram, in bases
     */
    private int getSize() {
        return end - start;
    }

    public String getPreferredFilename () {
        return String.format("%s%09d-%09ds%d.%s", filenamePrefix, getStart(), getEnd(),
            getBasesPerPixel(), FILE_EXT);
    }

    public ContextMapDiagram getDiagram() {
        return this.diagram;
    }

    /**
     * Configure this diagram to render as a thumbnail
     *
     * @param maxWidth the maximum width, in pixels, of the rendered thumbnail
     * @return this object
     */
    public RenderedContextMap asThumbnail(int maxWidth) {
        setMaxWidth(maxWidth);
        setScaleTrackHeight(1);
        setGeneTrackHeight(2);
        setExonRectHeght(2);
        setIntronRectHeight(2);
        setTickDistances(0, 0);
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
        return width;
    }

    /**
     * Constrain the diagram to fit within a fixed width,
     * by adjusting the scale. The resulting width will
     * be as close as possible to maxWidth, but no larger.
     *
     * @param maxWidth the maximum allowed width, in pixels
     * @return the actual width of the diagram
     */
    public int setMaxWidth(int maxWidth) {
        if (getSize() % maxWidth == 0)
            setBasesPerPixel(getSize() / maxWidth);
        else
            setBasesPerPixel((getSize() / maxWidth) + 1);

        assert this.width <= maxWidth;
        return this.width;
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
    public int getExonRectHeght() {
        return exonRectHeght;
    }

    /**
     * Set the height of the rectangles used to represent exons in this diagram
     * @param exonRectHeght the height in pixels
     */
    public void setExonRectHeght(int exonRectHeght) {
        this.exonRectHeght = exonRectHeght;
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
    * @param exonRectHeght the height in pixels
    */
    public void setIntronRectHeight(int intronRectHeight) {
        this.intronRectHeight = intronRectHeight;
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
     * must be a multiple of the <code>minorTickDistance</code>.
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
    }

    /**
     * Get the distance between major (labelled) ticks on the scale
     * track of this diagram. Always a multiple of the minor tick distance.
     * @return
     */
    public int getMajorTickDistance() {
        return majorTickDistance;
    }

    public void writeTo(OutputStream out) throws IOException {

        logger.debug(String.format("Drawing RenderedContextMap with dimensions %dx%d", width, getHeight()));

        switch (colorModel) {
        case DIRECT:
            image = new BufferedImage(width, getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            labelBuffer = null;
            break;
        case INDEXED:
            image = new BufferedImage(width, getHeight(),
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

        drawScaleTrack();

        if (labelGraf != null)
            labelGraf.dispose();

        for (int i = 1; i <= Math.min(numberOfPositiveTracks, diagram.numberOfPositiveTracks()); i++)
            drawGeneTrack(i, diagram.getTrack(i));

        for (int i = 1; i <= Math.min(numberOfNegativeTracks, diagram.numberOfNegativeTracks()); i++)
            drawGeneTrack(-i, diagram.getTrack(-i));

        ImageIO.write(image, FILE_FORMAT, out);

        image = null;
        graf.dispose();
        graf = null;
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
        this.width = getSize() / basesPerPixel;
    }



    private int yCoordinateOfAxis() {
        if (majorTickDistance == 0)
            return topOfTrack(0) + scaleTrackHeight / 2;
        return topOfTrack(0) + SCALE_VERTICAL_POS;
    }

    private void drawScaleTrack() {
        graf.setColor(scaleColor);
        graf.drawLine(xCoordinate(getStart()), yCoordinateOfAxis(),
            xCoordinate(getEnd()), yCoordinateOfAxis());

        if (minorTickDistance == 0 || majorTickDistance == 0)
            return;

        int majorTicksEvery = (majorTickDistance / minorTickDistance);
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
        drawScaleTick(pos, MAJOR_TICK_HEIGHT);
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

    private static final Color transparentColor = new Color(0,0,0,0);
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
            labelGraf.setColor(transparentColor);
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
        else if (x+w > this.width) {
            destWidth = this.width - x;
            x = this.width - destWidth;
        }
        if (y + h > image.getHeight())
            h = image.getHeight() - y;
        image.setRGB(x, y, destWidth, h, labelData, offset, w);
    }

    private void drawScaleTick(int pos, int tickHeight) {
        int topOfTick    = yCoordinateOfAxis() - (tickHeight / 2);
        int bottomOfTick = yCoordinateOfAxis() + (tickHeight / 2);

        int x = xCoordinate(pos);
        graf.drawLine(x, topOfTick, x, bottomOfTick);
    }

    private void drawGeneTrack(int trackNumber, List<Transcript> transcripts) {
        logger.debug(String.format("Drawing track %d", trackNumber));
        for (Transcript transcript : transcripts) {
            Color color = ArtemisColours.getColour(transcript.getColourId());
            logger.debug("Setting colour: " + color);
            graf.setColor(color);
            drawTranscript(trackNumber, transcript);
        }
    }

    private void drawTranscript(int trackNumber, Transcript transcript) {
        logger.debug(String.format("Drawing transcript %s (%d..%d) on track %d", transcript
                .getName(), transcript.getFmin(), transcript.getFmax(), trackNumber));
        int currentPos = transcript.getFmin();
        for (Exon exon : transcript.getExons()) {
            drawIntron(trackNumber, currentPos, exon.getStart());
            drawExon(trackNumber, exon.getStart(), exon.getEnd());
            currentPos = exon.getEnd();
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

    private void drawExon(int trackNumber, int start, int end) {
        if (end <= start) {
            logger.warn("Drawing nothing: empty or negative");
            return;
        }
        int x = xCoordinate(start), y = topOfExon(trackNumber);
        int width = pixelWidth(start, end);
        graf.fillRect(x, y, width, exonRectHeght);
    }

    /**
     * Calculate the x-position (in pixels relative to this diagram)
     * corresponding to a particular chromosome location.
     *
     * @param loc the chromosome location
     * @return the corresponding x position
     */
    private int xCoordinate(int loc) {
        return pixelWidth(getStart(), loc);
    }

    /**
     * Calculate the width in pixels of a segment of the diagram.
     *
     * @param start the start location, in interbase coordinates
     * @param end the end location, in interbase coordinates
     * @return the width in pixels
     */
    private int pixelWidth(int start, int end) {
        return (int) (Math.round((double) end / basesPerPixel) - Math.round((double) start
                / basesPerPixel));
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
        return topOfTrack(trackNumber) + (geneTrackHeight - exonRectHeght) / 2;
    }
}
