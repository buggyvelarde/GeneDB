package org.genedb.web.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
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

    private static final String FILE_FORMAT = "png";
    static final String FILE_EXT = "png";

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
    private int scaleTrackHeight = 20;
        
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
     * Font used for printing figures on the scale track
     */
    private static final Font LABEL_FONT = new Font("Lucida Sans", Font.PLAIN, 10);
    
    /**
     * How much space to leave between a major scale tick and the label below, in pixels.
     */
    private static final int LABEL_SEP = 2;


    private ContextMapDiagram diagram;
    private int width, height;

    private BufferedImage image;
    private Graphics2D graf;

    public RenderedContextMap(ContextMapDiagram diagram) {
        this.diagram = diagram;
        this.width = diagram.getSize() / basesPerPixel;
        this.height = scaleTrackHeight + diagram.numberOfTracks() * geneTrackHeight;
    }

    public ContextMapDiagram getDiagram() {
        return this.diagram;
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
        if (this.width % maxWidth == 0)
            setBasesPerPixel(this.width / maxWidth);
        else
            setBasesPerPixel((this.width / maxWidth) + 1);

        assert this.width <= maxWidth;
        return this.width;
    }
    
    /**
     * Get the height in pixels
     * @return the height in pixels of this rendered diagram
     */
    public int getHeight() {
        return this.height;
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
        if (majorTickDistance % minorTickDistance != 0)
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

        logger.debug(String.format("Drawing RenderedContextMap with dimensions %dx%d", width, height));
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        graf = (Graphics2D) image.getGraphics();

        drawScaleTrack();

        for (int i = 1; i <= diagram.numberOfPositiveTracks(); i++)
            drawGeneTrack(i, diagram.getTrack(i));

        for (int i = 1; i <= diagram.numberOfNegativeTracks(); i++)
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
        this.width = diagram.getSize() / basesPerPixel;
    }
    
    
    

    private int yCoordinateOfAxis() {
        return topOfTrack(0) + SCALE_VERTICAL_POS;
    }

    private void drawScaleTrack() {
        graf.setColor(Color.BLACK);
        graf.drawLine(xCoordinate(diagram.getStart()), yCoordinateOfAxis(),
            xCoordinate(diagram.getEnd()), yCoordinateOfAxis());
        
        if (minorTickDistance == 0 || majorTickDistance == 0)
            return;
        
        int majorTicksEvery = (majorTickDistance / minorTickDistance);
        int tickNumber = 0;
        int pos = majorTickDistance * (diagram.getStart() / majorTickDistance);
        while (pos < diagram.getEnd()) {
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
        
        graf.setFont(LABEL_FONT);
        graf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        FontRenderContext fontRenderContext = graf.getFontRenderContext();
        
        Font font = graf.getFont();
        String posString = String.valueOf(pos);
        int posHalfWidth = (int) font.getStringBounds(posString, fontRenderContext).getCenterX();
        LineMetrics posMetrics = font.getLineMetrics(posString, fontRenderContext);
        
        int x = xCoordinate(pos) - posHalfWidth;
        int y = yCoordinateOfAxis() + (MAJOR_TICK_HEIGHT / 2) + LABEL_SEP + (int) posMetrics.getAscent();
        graf.drawString(posString, x, y);
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
     * Calculate the x-position (i.e. in pixels relative to this diagram)
     * corresponding to a particular chromosome location.
     * 
     * @param loc the chromosome location
     * @return the corresponding x position
     */
    private int xCoordinate(int loc) {
        return pixelWidth(diagram.getStart(), loc);
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
        int firstGuess = (diagram.numberOfPositiveTracks() - trackNumber) * geneTrackHeight;

        // The first guess is right for non-negative tracks.
        // Also, it's always right as long as the scale track is the same height
        // as the gene tracks.
        // (The compiler should be able to optimise this away completely in that
        // case.)
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
