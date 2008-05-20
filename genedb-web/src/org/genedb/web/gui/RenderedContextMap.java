package org.genedb.web.gui;

import java.awt.Color;
import java.awt.Graphics2D;
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
 * colour, centred vertically within the gene track. An intron is represented
 * by a narrower rectangle of the same colour, also vertically centred and
 * continuous with the exons it separates.
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
    private static final int GENE_TRACK_HEIGHT  = 30;
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
    private static final int SCALE_TRACK_HEIGHT = 30;

    private ContextMapDiagram diagram;
    private int width, height;
    
    private BufferedImage image;
    private Graphics2D graf;

    public RenderedContextMap(ContextMapDiagram diagram) {
        this.diagram = diagram;
        this.width  = diagram.getSize() / BASES_PER_PIXEL;
        this.height = SCALE_TRACK_HEIGHT + diagram.numberOfTracks() * GENE_TRACK_HEIGHT;

        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        this.graf = (Graphics2D) image.getGraphics();
    }
    
    public ContextMapDiagram getDiagram() {
        return this.diagram;
    }

    public void writeTo(OutputStream out) throws IOException {

        drawScaleTrack();
        
        for (int i=1; i <= diagram.numberOfPositiveTracks(); i++)
            drawGeneTrack(i, diagram.getTrack(i));

        for (int i=1; i <= diagram.numberOfNegativeTracks(); i++)
            drawGeneTrack(-i, diagram.getTrack(-i));

        ImageIO.write(image, FILE_FORMAT, out);
    }
    
    private void drawScaleTrack() {
        //TODO
    }
    private void drawGeneTrack(int trackNumber, List<Transcript> transcripts) {
        logger.debug(String.format("Drawing track %d", trackNumber));
        for (Transcript transcript: transcripts) {
            Color color = ArtemisColours.getColour(transcript.getColourId());
            logger.debug("Setting colour: "+color);
            graf.setColor(color);
            drawTranscript(trackNumber, transcript);
        }
    }
    private void drawTranscript(int trackNumber, Transcript transcript) {
        logger.debug(String.format("Drawing transcript %s (%d..%d) on track %d",
            transcript.getName(), transcript.getFmin(), transcript.getFmax(), trackNumber));
        int currentPos = transcript.getFmin();
        for (Exon exon: transcript.getExons()) {
            drawIntron(trackNumber, currentPos, exon.getStart());
            drawExon(trackNumber, exon.getStart(), exon.getEnd());
            currentPos = exon.getEnd();
        }
        drawIntron(trackNumber, currentPos, transcript.getFmax());
    }
    private void drawIntron(int trackNumber, int start, int end) {
        logger.debug(String.format("About to draw intron %d..%d, track %d",
                start, end, trackNumber));
        if (end <= start) {
            logger.debug("Drawing nothing: empty or negative");
            return;
        }
        int x = xCoordinate(start), y = topOfIntron(trackNumber);
        int width = pixelWidth(start, end);
        logger.debug(String.format("Drawing rectangle at (%d,%d), width %d, height %d",
            x, y, width, INTRON_RECT_HEIGHT));
        graf.fillRect(x, y, width, INTRON_RECT_HEIGHT);
    }
    private void drawExon(int trackNumber, int start, int end) {
        logger.debug(String.format("About to draw exon %d..%d, track %d",
            start, end, trackNumber));
    if (end <= start) {
        logger.debug("Drawing nothing: empty or negative");
        return;
    }
    int x = xCoordinate(start), y = topOfExon(trackNumber);
    int width = pixelWidth(start, end);
    logger.debug(String.format("Drawing rectangle at (%d,%d), width %d, height %d",
        x, y, width, EXON_RECT_HEIGHT));
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
    private int pixelWidth (int start, int end) {
        return (int) (Math.round( (double) end / BASES_PER_PIXEL)
            - Math.round( (double) start / BASES_PER_PIXEL));
    }
    /**
     * Calculate the y-coordinate of the top of a track.
     * @param trackNumber
     * @return
     */
    private int topOfTrack(int trackNumber) {
        int firstGuess = (diagram.numberOfPositiveTracks() - trackNumber) * GENE_TRACK_HEIGHT;

        // The first guess is right for non-negative tracks.
        // Also, it's always right as long as the scale track is the same height as the gene tracks.
        // (The compiler should be able to optimise this away completely in that case.)
        if (SCALE_TRACK_HEIGHT == GENE_TRACK_HEIGHT || trackNumber >= 0)
            return firstGuess;

        // Otherwise, correct for the differing height of the scale track.
        return firstGuess - GENE_TRACK_HEIGHT + SCALE_TRACK_HEIGHT;
    }
    private int topOfIntron(int trackNumber) {
        return topOfTrack(trackNumber) + (GENE_TRACK_HEIGHT - INTRON_RECT_HEIGHT)/2;
    }
    private int topOfExon(int trackNumber) {
        return topOfTrack(trackNumber) + (GENE_TRACK_HEIGHT - EXON_RECT_HEIGHT)/2;
    }
}
