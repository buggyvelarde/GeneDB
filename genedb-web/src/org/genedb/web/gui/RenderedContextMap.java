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

    private static final int BASES_PER_PIXEL = 10;

    /**
     * The height of a gene track, in pixels.
     */
    private static final int GENE_TRACK_HEIGHT = 20;
    /**
     * The height of the rectangle representing an exon
     */
    private static final int EXON_RECT_HEIGHT = 12;
    /**
     * The height of the rectangle representing an intron
     */
    private static final int INTRON_RECT_HEIGHT = 2;

    /**
     * The height of the scale track.
     */
    private static final int SCALE_TRACK_HEIGHT = 20;
        
    /**
     * Distance between minor scale ticks, in bases
     */
    private static final int MINOR_TICK_DISTANCE = 200;
    
    /**
     * Distance between major scale ticks, in bases.
     * Must be a multiple of <code>MINOR_TICK_DISTANCE</code>.
     */
    private static final int MAJOR_TICK_DISTANCE = 1000;
    
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
        this.width = diagram.getSize() / BASES_PER_PIXEL;
        this.height = SCALE_TRACK_HEIGHT + diagram.numberOfTracks() * GENE_TRACK_HEIGHT;

        logger.debug(String.format("RenderedContextMap has dimensions %dx%d", width, height));
    }

    public ContextMapDiagram getDiagram() {
        return this.diagram;
    }
    
    public int getHeight() {
        return this.height;
    }

    public void writeTo(OutputStream out) throws IOException {
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
        return BASES_PER_PIXEL;
    }
    
    private int yCoordinateOfAxis() {
        return topOfTrack(0) + SCALE_VERTICAL_POS;
    }

    private void drawScaleTrack() {
        graf.setColor(Color.BLACK);
        graf.drawLine(xCoordinate(diagram.getStart()), yCoordinateOfAxis(),
            xCoordinate(diagram.getEnd()), yCoordinateOfAxis());
        
        int majorTicksEvery = (MAJOR_TICK_DISTANCE / MINOR_TICK_DISTANCE);
        int tickNumber = 0;
        for(int pos=diagram.getStart(); pos < diagram.getEnd(); pos += MINOR_TICK_DISTANCE) {
            if (tickNumber++ % majorTicksEvery == 0)
                drawMajorScaleTick(pos);
            else
                drawMinorScaleTick(pos);
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
        graf.fillRect(x, y, width, INTRON_RECT_HEIGHT);
    }

    private void drawExon(int trackNumber, int start, int end) {
        if (end <= start) {
            logger.warn("Drawing nothing: empty or negative");
            return;
        }
        int x = xCoordinate(start), y = topOfExon(trackNumber);
        int width = pixelWidth(start, end);
        graf.fillRect(x, y, width, EXON_RECT_HEIGHT);
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
     * Convert a width in bases to a width in pixels.
     * 
     * @param baseWidth the width in bases
     * @return the width in pixels
     */
    private int pixelWidth(int start, int end) {
        return (int) (Math.round((double) end / BASES_PER_PIXEL) - Math.round((double) start
                / BASES_PER_PIXEL));
    }

    /**
     * Calculate the y-coordinate of the top of a track.
     * 
     * @param trackNumber
     * @return
     */
    private int topOfTrack(int trackNumber) {
        int firstGuess = (diagram.numberOfPositiveTracks() - trackNumber) * GENE_TRACK_HEIGHT;

        // The first guess is right for non-negative tracks.
        // Also, it's always right as long as the scale track is the same height
        // as the gene tracks.
        // (The compiler should be able to optimise this away completely in that
        // case.)
        if (SCALE_TRACK_HEIGHT == GENE_TRACK_HEIGHT || trackNumber >= 0)
            return firstGuess;

        // Otherwise, correct for the differing height of the scale track.
        return firstGuess - GENE_TRACK_HEIGHT + SCALE_TRACK_HEIGHT;
    }

    private int topOfIntron(int trackNumber) {
        return topOfTrack(trackNumber) + (GENE_TRACK_HEIGHT - INTRON_RECT_HEIGHT) / 2;
    }

    private int topOfExon(int trackNumber) {
        return topOfTrack(trackNumber) + (GENE_TRACK_HEIGHT - EXON_RECT_HEIGHT) / 2;
    }
}
