//package org.genedb.web.gui;
//
//import org.genedb.db.domain.objects.CompoundLocatedFeature;
//import org.genedb.db.domain.objects.Gap;
//import org.genedb.db.domain.objects.LocatedFeature;
//import org.genedb.web.mvc.model.BerkeleyMapFactory;
//
//import org.gmod.schema.feature.Region;
//
//import org.apache.log4j.Logger;
//
//import net.sf.json.JSONString;
//
//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.font.FontRenderContext;
//import java.awt.font.LineMetrics;
//import java.awt.geom.Rectangle2D;
//import java.awt.image.BufferedImage;
//import java.awt.image.IndexColorModel;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.NavigableMap;
//import java.util.SortedSet;
//import java.util.TreeMap;
//
//import javax.imageio.ImageIO;
//
//public abstract class RenderedDiagram {
//    private static final Logger logger = Logger.getLogger(RenderedDiagram.class);
//
//    private static final int MAX_HEIGHT=5000;
//
//    protected static final String FILE_FORMAT = "png";
//    protected static final String FILE_EXT = "png";
//    protected boolean thumbNailMode;
//
//    private BerkeleyMapFactory bmf;
//
//    public void setBerkelyMapFactory(BerkeleyMapFactory bmf) {
//        this.bmf = bmf;
//    }
//
//    protected enum ColorModel { DIRECT, INDEXED }
//
//    public class Tile implements JSONString {
//        private int width;
//        private String key;
//
//        public Tile(int width, String key) {
//            this.width = width;
//            this.key = key;
//        }
//
//        public String toJSONString() {
//            return String.format("[%d,\"%s\"]", width, key);
//        }
//    }
//
//    // Configuration members
//
//    /**
//     * What color model should be used?
//     */
//    protected ColorModel colorModel = ColorModel.INDEXED;
//
//    /*
//     * These are used for the label buffer, when the INDEXED color model is in use.
//     */
//    private static final int MAX_LABEL_WIDTH  = 100;
//    private static final int MAX_LABEL_HEIGHT = 50;
//
//    /**
//     * The scale at which this diagram is drawn, in bases per pixel.
//     */
//    protected double basesPerPixel = 10.0;
//
//    /**
//     * The horizontal distance in pixels from where the axis break slash crosses the
//     * axis to its farthest extent above-right or below-left.
//     */
//    private int axisBreakX = 2;
//
//    /**
//     * The vertical distance in pixels from where the axis break slash crosses the
//     * axis to its farthest extent above-right or below-left.
//     */
//    private int axisBreakY = 4;
//
//    /**
//     * The horizontal distance in pixels between the two slashes that denote
//     * a break in the axis.
//     */
//    private int axisBreakGap = 6;
//
//    /**
//     * The height of the scale track.
//     */
//    private int scaleTrackHeight = 22;
//
//    /**
//     * The height of an ordinary track, in pixels.
//     */
//    private int trackHeight = 20;
//
//    /**
//     * The width, in pixels, of the left-hand margin.
//     */
//    private int leftMargin = 0;
//
//    /**
//     * The width, in pixels, of the right-hand margin.
//     */
//    private int rightMargin = 0;
//
//    /**
//     * The colour of the scale.
//     */
//    private Color scaleColor = Color.GRAY;
//
//    /**
//     * The colour of the labels.
//     */
//    private Color labelColor = Color.BLACK;
//
//    /**
//     * The colour of the label background. If <code>null</code>, no label background is printed.
//     * (Note that LCD text antialiasing doesn't work on a transparent background. Also note
//     * that IE6 does not ordinarily render partial transparency, though there is a workaround
//     * using AlphaImageLoader.)
//     */
//    private Color labelBackgroundColor = null;
//
//    /**
//     * Font used for printing figures on the scale track
//     */
//    private Font labelFont;
//
//
//    /**
//     * Distance between minor scale ticks, in bases
//     */
//    private int minorTickDistance = 200;
//
//    /**
//     * Distance between major scale ticks, in bases.
//     * Must be a multiple of <code>MINOR_TICK_DISTANCE</code>.
//     */
//    private int majorTickDistance = 1000;
//
//    /**
//     * Height of each minor scale tick above the axis, in pixels
//     */
//    private int minorTickHeightAbove = 2;
//
//    /**
//     * Height of each minor scale tick below the axis, in pixels
//     */
//    private int minorTickHeightBelow = 2;
//
//    /**
//     * Height of each major scale tick above the axis, in pixels
//     */
//    private int majorTickHeightAbove = 4;
//
//    /**
//     * Height of each major scale tick below the axis, in pixels
//     */
//    private int majorTickHeightBelow = 4;
//
//    /**
//     * Height of the scale boundary markers, above and below, in pixels.
//     */
//    private int boundaryTickHeight = 6;
//
//    /**
//     * Vertical position of scale axis within scale track,
//     * measured in pixels downwards from the top of the scale track.
//     */
//    private int scaleVerticalPos = majorTickHeightAbove;
//
//    /**
//     * How much space to leave between a major scale tick and the label below, in pixels.
//     */
//    private static final int labelSep = 2;
//
//
//    // Protected members
//    protected Graphics2D graf;
//
//
//    // Private members. Should be considered as implementation details.
//
//    private Graphics2D labelGraf;
//
//    private int numberOfPositiveTracks;
//    private int numberOfNegativeTracks;
//
//    protected TrackedDiagram diagram;
//    private NavigableMap<Integer,Gap> gapsByStart = new TreeMap<Integer,Gap>();
//    private int start;
//    private int end;
//    private int sizeExcludingGaps;
//
//    private BufferedImage image;
//    private BufferedImage labelBuffer;
//
//    protected RenderedDiagram(TrackedDiagram diagram) {
//        this.diagram = diagram;
//        this.start = diagram.getStart();
//        this.end = diagram.getEnd();
//
//        this.numberOfPositiveTracks = diagram.numberOfPositiveTracks();
//        this.numberOfNegativeTracks = diagram.numberOfNegativeTracks();
//    }
//
//    protected void addGap(Gap gap) {
//        gapsByStart.put(gap.getFmin(), gap);
//    }
//
//    protected Iterable<Gap> getGaps() {
//        return gapsByStart.values();
//    }
//
//    /**
//     * Compute the size, excluding gaps, between
//     * <code>start</code> and <code>end</code>,
//     * and store the result in <code>sizeExcludingGaps</code>.
//     */
//    protected void calculateSizeExcludingGaps() {
//        sizeExcludingGaps = 0;
//        int loc = getStart();
//        for (Gap gap : gapsByStart.values()) {
//            if (loc < gap.getFmin()) {
//                sizeExcludingGaps += gap.getFmin() - loc;
//                loc = gap.getFmax();
//            }
//        }
//
//        if (loc < getEnd())
//            sizeExcludingGaps += getEnd() - loc;
//    }
//
//    public RenderedDiagram restrict(int start, int end) {
//        if (start < diagram.getStart()) {
//            logger.warn(String.format("Start of diagram is %d, start of restriction is %d",
//                diagram.getStart(), start));
//            this.start = diagram.getStart();
//        }
//        else
//            this.start = start;
//
//        if (end > diagram.getEnd()) {
//            logger.warn(String.format("End of diagram is %d, end of restriction is %d",
//                diagram.getEnd(), end));
//            this.end = diagram.getEnd();
//        }
//        else
//            this.end = end;
//
//        return this;
//    }
//
//    abstract String getKey();
//
//
//    /**
//     * Get the location of the start of the diagram.
//     *
//     * @return the start of the diagram, in interbase coordinates
//     */
//    public int getStart() {
//        return start;
//    }
//
//    /**
//     * Get the location of the end of the diagram.
//     *
//     * @return the end of the diagram, in interbase coordinates
//     */
//    public int getEnd() {
//        return end;
//    }
//
//
////    public abstract String getPreferredFilename();
//
////    private String getPreferredFilenameForTile(int tileIndex, int tileWidth) {
////        return String.format("%s%09d-%09ds%.0ft%dw%d.%s", filenamePrefix, getStart(), getEnd(),
////            getBasesPerPixel(), tileIndex, tileWidth, FILE_EXT);
////    }
//
//    public TrackedDiagram getDiagram() {
//        return this.diagram;
//    }
//
//    /**
//     * Configure this diagram to render as a thumbnail.
//     *
//     * @param maxWidth the maximum width, in pixels, of the rendered thumbnail
//     * @return this object
//     */
//    public RenderedDiagram asThumbnail(int maxWidth) {
//        setMaxWidth(maxWidth, 0);
//        setScaleTrackHeight(1);
//        setTrackHeight(2);
//        setAxisBreakSlash(0, 0);
//        thumbNailMode=true;
//        forceTracks(2, 2);
//
//         /* For thumbnails, the resulting file is usually smaller with a direct color model.
//          * This means we have to apply the AlphaImageLoader hack to the chromosome thumbnail
//          * for IE6. */
//        this.colorModel = ColorModel.DIRECT;
//
//        return this;
//    }
//
//    /**
//     * Get the width in pixels
//     * @return the width in pixels of this rendered diagram
//     */
//    public int getWidth() {
//        return leftMargin + pixelWidth(getStart(), getEnd()) + rightMargin;
//    }
//
//    /**
//     * Constrain the diagram to fit within a fixed width,
//     * for a given gap size, by adjusting the scale. The
//     * resulting width will be as close as possible to maxWidth,
//     * but no larger. The <code>axisBreakGap</code> is also
//     * set equal to the supplied value.
//     *
//     * @param maxWidth the maximum allowed width, in pixels
//     * @param axisBreakGap the size, in pixels, of a break in the axis
//     *          representing a gap.
//     * @return the actual width of the diagram
//     */
//    public int setMaxWidth(int maxWidth, int axisBreakGap) {
//        this.axisBreakGap = axisBreakGap;
//        calculateSizeExcludingGaps();
//
//        int maxWidthExcludingGaps = maxWidth - axisBreakGap * this.gapsByStart.size();
//        setBasesPerPixel(sizeExcludingGaps, maxWidthExcludingGaps);
//
//        calculateContigs();
//        assert getWidth() <= maxWidth;
//        return getWidth();
//    }
//
//    /**
//     * Get the height in pixels
//     * @return the height in pixels of this rendered diagram
//     */
//    public int getHeight() {
//        return scaleTrackHeight + numberOfTracks() * trackHeight;
//    }
//
//    /**
//     * How many tracks will this diagram have, when rendered?
//     * May be different from getDiagram().numberOfTracks(),
//     * if {@link #forceTracks(int,int)} has been used.
//     *
//     * @return
//     */
//    private int numberOfTracks() {
//        return numberOfPositiveTracks + numberOfNegativeTracks;
//    }
//
//    /**
//     * Force the rendered diagram to have the specified number of tracks.
//     * Thus there may be empty tracks in the rendered diagram,
//     * and higher-numbered tracks will not be shown.
//     *
//     * @param positiveTracks
//     * @param negativeTracks
//     */
//    private void forceTracks(int positiveTracks, int negativeTracks) {
//        this.numberOfPositiveTracks = positiveTracks;
//        this.numberOfNegativeTracks = negativeTracks;
//    }
//
//    /**
//     * Get the height of the ordinary tracks of this diagram.
//     * @return the height in pixels
//     */
//    public int getTrackHeight() {
//        return trackHeight;
//    }
//
//    /**
//     * Set the height of the ordinary tracks of this diagram.
//     * @param trackHeight the height in pixels
//     */
//    public void setTrackHeight(int trackHeight) {
//        this.trackHeight = trackHeight;
//    }
//
//    /**
//     * Get the height of the scale track of this diagram.
//     * @return the height in pixels
//     */
//    public int getScaleTrackHeight() {
//        return scaleTrackHeight;
//    }
//
//    /**
//     * Set the height of the scale track.
//     * @param scaleTrackHeight the height in pixels
//     */
//    public void setScaleTrackHeight(int scaleTrackHeight) {
//        this.scaleTrackHeight = scaleTrackHeight;
//    }
//
//    protected void setAxisBreakSlash(int axisBreakX, int axisBreakY) {
//        this.axisBreakX = axisBreakX;
//        this.axisBreakY = axisBreakY;
//    }
//
//    protected void setAxisBreakGap(int axisBreakGap) {
//        this.axisBreakGap = axisBreakGap;
//    }
//
//    protected int getMinorTickHeightAbove() {
//        return minorTickHeightAbove;
//    }
//
//    protected void setMinorTickHeightAbove(int minorTickHeightAbove) {
//        this.minorTickHeightAbove = minorTickHeightAbove;
//    }
//
//    protected int getMinorTickHeightBelow() {
//        return minorTickHeightBelow;
//    }
//
//    protected void setMinorTickHeightBelow(int minorTickHeightBelow) {
//        this.minorTickHeightBelow = minorTickHeightBelow;
//    }
//
//    protected int getMajorTickHeightAbove() {
//        return majorTickHeightAbove;
//    }
//
//    protected void setMajorTickHeightAbove(int majorTickHeightAbove) {
//        this.majorTickHeightAbove = majorTickHeightAbove;
//    }
//
//    protected int getMajorTickHeightBelow() {
//        return majorTickHeightBelow;
//    }
//
//    protected void setMajorTickHeightBelow(int majorTickHeightBelow) {
//        this.majorTickHeightBelow = majorTickHeightBelow;
//    }
//
//    protected void setBoundaryTickHeight(int boundaryTickHeight) {
//        this.boundaryTickHeight = boundaryTickHeight;
//    }
//
//    //public abstract String getRelativeRenderDirectory();
//
//    public void setLabelFont(Font labelFont) {
//        this.labelFont = labelFont;
//    }
//
//    abstract public String getKeyForTile(int index, int start, int width);
//
//    public List<RenderedContextMap.Tile> renderTiles(int tileWidth) throws ImageCreationException {
//
//        beforeRender();
//        drawScaleTrack();
//        renderFeatures();
//
//        List<RenderedContextMap.Tile> tiles = new ArrayList<RenderedContextMap.Tile>();
//
//        if (getWidth() != image.getWidth())
//            throw new IllegalStateException(String.format(
//                "Image width: expected=%d, actual=%d", getWidth(), image.getWidth()));
//
//        for (int startOfTile = 0, tileIndex = 1; startOfTile < getWidth(); startOfTile += tileWidth, tileIndex++) {
//
//            int widthOfThisTile = tileWidth;
//            if (startOfTile + widthOfThisTile > getWidth()) {
//                widthOfThisTile = getWidth() - startOfTile;
//            }
//
//            String key = getKeyForTile(tileIndex, startOfTile, tileWidth);
//
//            logger.debug(String.format("Rendering tile %d (start=%d,width=%d) to '%s'", tileIndex, startOfTile, widthOfThisTile, key));
//            BufferedImage tile = image.getSubimage(startOfTile, 0, widthOfThisTile, image.getHeight());
//
//            try {
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                ImageIO.write(tile, FILE_FORMAT, out);
//                out.close();
//                bmf.getImageMap().put(key, out.toByteArray());
//            } catch (IOException exp) {
//                throw new RuntimeException(String.format("Failed to write image file '%s'", key), exp);
//            }
//
//
//            tiles.add(new Tile(widthOfThisTile, key));
//        }
//
//        return tiles;
//    }
//
//    /**
//     * A navigable map from base -> pixel, with an entry corresponding to
//     * the beginning of each contiguous region. It is populated at the start
//     * of rendering, by {@link #drawScaleLine()}, and used for the duration
//     * of the rendering process.
//     */
//    private NavigableMap<Integer,Double> contigs;
//
//    private static final Color TRANSPARENT = new Color(0,0,0,0);
//
//    private void calculateContigs() {
//        contigs = new TreeMap<Integer,Double>();
//        double x = xCoordinate(getStart());
//        for (Gap gap: gapsByStart.values()) {
//            x = xCoordinateAsDoubleIgnoringMargin(gap.getFmin()) + axisBreakGap;
//            contigs.put(gap.getFmax(), x);
//        }
//    }
//
//    /**
//     * The IndexColorModel to use in INDEXED mode.
//     * Subclasses should override this method to return
//     * an 8-bit <code>IndexColorModel</code> adequate to
//     * represent all the colours used on the rendered
//     * diagram.
//     *
//     * @return An 8-bit <code>IndexColorModel</code>
//     */
//    protected abstract IndexColorModel byteIndexedColorModel();
//
//    protected void beforeRender() throws ImageCreationException {
//        calculateContigs();
//        if (getHeight() > MAX_HEIGHT) {
//            // Abort
//            image = null;
//            throw new ImageCreationException(String.format("Height requested '%d' is unreasonable", getHeight()));
//        }
//        switch (colorModel) {
//        case DIRECT:
//            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
//            labelBuffer = null;
//            break;
//        case INDEXED:
//            logger.info(String.format("About to try and create a BufferedImage of '%d' x '%d'", getWidth(), getHeight()));
//            image = new BufferedImage(getWidth(), getHeight(),
//                BufferedImage.TYPE_BYTE_INDEXED, byteIndexedColorModel());
//            break;
//        }
//        graf = (Graphics2D) image.getGraphics();
//
//        if (colorModel == ColorModel.INDEXED) {
//            labelBuffer = new BufferedImage(MAX_LABEL_WIDTH, MAX_LABEL_HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
//            labelGraf = (Graphics2D) labelBuffer.getGraphics();
//            labelGraf.setComposite(AlphaComposite.Src);
//            setLabelRenderingHints(labelGraf);
//            labelGraf.setFont(labelFont);
//        }
//    }
//
//    protected void afterRender() {
//        image = null;
//        graf.dispose();
//        graf = null;
//    }
//
//    public void writeTo(OutputStream out) throws ImageCreationException, IOException {
//
//        logger.debug(String.format("Drawing diagram (%s) with dimensions %dx%d", getClass(), getWidth(), getHeight()));
//
//        beforeRender();
//        drawScaleTrack();
//        renderFeatures();
//
//        ImageIO.write(image, FILE_FORMAT, out);
//
//        afterRender();
//    }
//
//    protected void renderFeatures() {
//        SortedSet<AllocatedCompoundFeature> features = diagram.getAllocatedCompoundFeatures();
//        for (AllocatedCompoundFeature superfeature : features) {
//            drawCompoundFeature(superfeature);
//            int track = superfeature.getTrack();
//            CompoundLocatedFeature compoundFeature = superfeature.getFeature();
//
//            if (compoundFeature.getFmax() < this.getStart() || compoundFeature.getFmin() > this.getEnd())
//                continue;
//
//            for (Collection<LocatedFeature> subfeatures: superfeature.getSubtracks()) {
//                for (LocatedFeature subfeature: subfeatures) {
//                    drawFeature(track, subfeature, superfeature);
//                }
//                track += Integer.signum(track);
//            }
//        }
//    }
//
//    /**
//     * This method is called to render a compound feature,
//     * before rendering its individual subfeatures. The
//     * default implementation does nothing; subclasses may
//     * override it for their own purposes.
//     *
//     * @param allocatedCompoundFeature the allocated compound feature to draw
//     */
//    protected void drawCompoundFeature(AllocatedCompoundFeature allocatedCompoundFeature) {
//        // empty
//    }
//
//    abstract protected void drawFeature(int track, LocatedFeature subfeature, AllocatedCompoundFeature superfeature);
//
//    /**
//     * Get the scale at which this diagram is drawn, in bases per pixel.
//     *
//     * @return
//     */
//    public double getBasesPerPixel() {
//        return basesPerPixel;
//    }
//
//    /**
//     * Set the scale at which this diagram should be drawn. Will adjust the
//     * width appropriately.
//     *
//     * @param basesPerPixel the new scale, in bases per pixel
//     */
//    public void setBasesPerPixel(int basesPerPixel) {
//        if (basesPerPixel <= 0)
//            throw new IllegalArgumentException(String.format("Cannot have %d bases per pixel!",
//                basesPerPixel));
//
//        this.basesPerPixel = basesPerPixel;
//    }
//
//    public void setBasesPerPixel(int bases, int pixels) {
//        logger.debug(String.format("Setting bases per pixel for bases=%d, pixels=%d", bases, pixels));
//        if (pixels <= bases) {
//            if (bases % pixels == 0) {
//                setBasesPerPixel(bases / pixels);
//            }
//            else {
//                setBasesPerPixel(1 + (bases / pixels));
//            }
//        }
//        else {
//            basesPerPixel = (double) 1 / (pixels / bases);
//        }
//    }
//
//    /**
//     * Calculate the width in pixels of a feature of the diagram.
//     *
//     * @param locatedFeature the feature in question
//     * @return the width in pixels
//     */
//    protected int pixelWidth(LocatedFeature locatedFeature) {
//        return pixelWidth(locatedFeature.getFmin(), locatedFeature.getFmax());
//    }
//
//    /**
//     * Calculate the width in pixels of a region.
//     *
//     * @param region the region in question
//     * @return the width in pixels
//     */
//    protected int pixelWidth(Region region) {
//        return pixelWidth(region.getFmin(), region.getFmax());
//    }
//
//    /**
//     * Calculate the width in pixels of a segment of the diagram.
//     *
//     * @param start the start location, in interbase coordinates
//     * @param end the end location, in interbase coordinates
//     * @return the width in pixels
//     */
//    protected int pixelWidth(int start, int end) {
//        return xCoordinate(end) - xCoordinate(start) + 1;
//    }
//
//    private double naivePixelWidth(int start, int end) {
//        double startPx = start / basesPerPixel;
//        double endPx   = end / basesPerPixel;
//
//        return (endPx - startPx);
//    }
//
//    protected Gap gapContainingLocation(int loc) {
//        Map.Entry<Integer, Gap> e = gapsByStart.lowerEntry(loc);
//        if (e == null || loc >= e.getValue().getFmax())
//            return null;
//        return e.getValue();
//    }
//
//    /**
//     * Calculate the x-position (in pixels relative to this diagram)
//     * corresponding to a particular chromosome location.
//     *
//     * @param loc the chromosome location
//     * @return the corresponding x position
//     */
//    protected int xCoordinate(int loc) {
//        return leftMargin + (int) Math.round(xCoordinateAsDoubleIgnoringMargin(loc));
//    }
//    private double xCoordinateAsDoubleIgnoringMargin(int loc) {
//        Gap gapContainingLocation = gapContainingLocation(loc);
//        if (gapContainingLocation != null) {
//            logger.warn(String.format("Diagram location %d lies in the gap '%s'", loc, gapContainingLocation.getUniqueName()));
//            Double gapEndPx = contigs.get(gapContainingLocation.getFmax());
//            if (gapEndPx == null)
//                throw new IllegalStateException("Failed to locate gap. This should never happen!");
//            return gapEndPx - axisBreakGap / 2;
//        }
//        Map.Entry<Integer, Double> e = contigs.floorEntry(loc);
//        if (e == null)
//            return naivePixelWidth(getStart(), loc);
//
//        return e.getValue() + naivePixelWidth(e.getKey(), loc);
//    }
//
//    /**
//     * Get the colour of the scale.
//     * @return the colour of the scale
//     */
//    public Color getScaleColor() {
//        return scaleColor;
//    }
//
//    /**
//     * Set the colour of the scale.
//     * @param scaleColor the colour of the scale
//     */
//    public void setScaleColor(Color scaleColor) {
//        this.scaleColor = scaleColor;
//    }
//
//    /**
//     * Get the colour of the labels on the scale track.
//     * @return the colour of the labels
//     */
//    public Color getLabelColor() {
//        return labelColor;
//    }
//
//    /**
//     * Set the colour of the labels on the scale track.
//     * @param labelColor the colour of the labels
//     */
//    public void setLabelColor(Color labelColor) {
//        this.labelColor = labelColor;
//    }
//
//    /**
//     * Get the distance between minor (unlabelled) ticks on the
//     * scale track of this diagram.
//     * @return the distance in bases
//     */
//    public int getMinorTickDistance() {
//        return minorTickDistance;
//    }
//
//    protected int getScaleVerticalPos() {
//        return scaleVerticalPos;
//    }
//
//    protected void setScaleVerticalPos(int scaleVerticalPos) {
//        this.scaleVerticalPos = scaleVerticalPos;
//    }
//
//    /**
//     * Set the distance between major (labelled) and minor (unlabelled) ticks on
//     * the scale track of this diagram. The <code>majorTickDistance</code>
//     * must be a multiple of the <code>minorTickDistance</code>. If the major
//     * tick distance is zero, then only minor ticks will be drawn. If the tick
//     * distances are both zero, then no scale ticks will be drawn.
//     *
//     * @param majorTickDistance the distance in bases
//     * @param minorTickDistance the distance in bases
//     */
//    public void setTickDistances(int majorTickDistance, int minorTickDistance) {
//        if (minorTickDistance != 0 && majorTickDistance % minorTickDistance != 0)
//            throw new IllegalArgumentException(String.format(
//                "Major tick distance %d is not a multiple of minor tick distance %d",
//                majorTickDistance, minorTickDistance));
//
//        this.majorTickDistance = majorTickDistance;
//        this.minorTickDistance = minorTickDistance;
//
//        if (majorTickDistance == 0)
//            scaleVerticalPos = 0;
//    }
//
//    /**
//     * Get the distance between major (labelled) ticks on the scale
//     * track of this diagram. Always a multiple of the minor tick distance.
//     * @return
//     */
//    public int getMajorTickDistance() {
//        return majorTickDistance;
//    }
//
//    /**
//     * Calculate the y-coordinate of the top of a track.
//     *
//     * @param trackNumber
//     * @return
//     */
//    protected int topOfTrack(int trackNumber) {
//        int firstGuess = (numberOfPositiveTracks - trackNumber) * trackHeight;
//
//        // The first guess is right for non-negative tracks.
//        // Also, it's always right as long as the scale track is the same height
//        // as the ordinary tracks.
//        if (scaleTrackHeight == trackHeight || trackNumber >= 0)
//            return firstGuess;
//
//        // Otherwise, correct for the differing height of the scale track.
//        return firstGuess - trackHeight + scaleTrackHeight;
//    }
//
//    /**
//     * Draw the scale line, marking any gaps. Does not draw ticks.
//     */
//    protected void drawScaleLine() {
//        graf.setColor(scaleColor);
//        int x = xCoordinate(getStart()), y = yCoordinateOfAxis();
//        for (Gap gap: gapsByStart.values()) {
//            int gapStartX = xCoordinate(gap.getFmin());
//
//            if (labelBackgroundColor != null && axisBreakGap > 0) {
//                graf.setColor(labelBackgroundColor);
//                graf.fillRect(gapStartX - axisBreakX - 1, y - axisBreakY - 1, 2 * axisBreakX + axisBreakGap + 3, 2 * axisBreakY + 3);
//                graf.setColor(scaleColor);
//            }
//
//            graf.drawLine(x, y, gapStartX, y);
//
//            if (axisBreakGap > 0) {
//                graf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                graf.drawLine(gapStartX + axisBreakX, y - axisBreakY, gapStartX - axisBreakX, y + axisBreakY);
//                graf.drawLine(gapStartX + axisBreakX + axisBreakGap, y - axisBreakY, gapStartX - axisBreakX + axisBreakGap, y + axisBreakY);
//                graf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//            }
//
//            x = xCoordinate(gap.getFmin()) + axisBreakGap;
//        }
//        graf.drawLine(x, y, xCoordinate(getEnd()), y);
//
//        drawBoundaryMarker(xCoordinate(getStart()));
//        drawBoundaryMarker(xCoordinate(getEnd()));
//    }
//
//    protected void drawBoundaryMarker(int x) {
//        if (boundaryTickHeight > 0) {
//            int y = yCoordinateOfAxis();
//            graf.drawLine(x, y - boundaryTickHeight, x, y + boundaryTickHeight);
//        }
//    }
//
//    protected int yCoordinateOfAxis() {
//        return topOfTrack(0) + scaleVerticalPos;
//    }
//
//    protected void drawScaleTrack() {
//
//        drawScaleLine();
//
//        if (minorTickDistance == 0)
//            return;
//
//        int majorTicksEvery = (majorTickDistance == 0 ? Integer.MAX_VALUE : (majorTickDistance / minorTickDistance));
//        int tickNumber = 0;
//        int pos = majorTickDistance * (getStart() / majorTickDistance);
//        while (pos <= getEnd()) {
//            if (tickNumber++ % majorTicksEvery == 0)
//                drawMajorScaleTick(pos);
//            else
//                drawMinorScaleTick(pos);
//            pos += minorTickDistance;
//        }
//    }
//
//    private void drawMinorScaleTick(int pos) {
//        drawScaleTick(pos, minorTickHeightAbove, minorTickHeightBelow);
//    }
//
//    private void drawMajorScaleTick(int pos) {
//        if (drawScaleTick(pos, majorTickHeightAbove, majorTickHeightBelow))
//            drawLabel(pos);
//    }
//
//    private void drawLabel(int pos) {
//        switch (colorModel) {
//        case DIRECT:
//            drawLabelDirectly(pos);
//            break;
//        case INDEXED:
//            drawLabelIndirectly(pos);
//        }
//    }
//
//    protected void setLabelRenderingHints(Graphics2D graf) {
//        graf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        graf.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//    }
//
//    private void drawLabelDirectly(int pos) {
//        graf.setFont(labelFont);
//        Color previousColor = graf.getColor();
//        setLabelRenderingHints(graf);
//
//        FontRenderContext fontRenderContext = graf.getFontRenderContext();
//        Font font = graf.getFont();
//        String labelString = String.valueOf(pos);
//        int labelHalfWidth = (int) font.getStringBounds(labelString, fontRenderContext).getCenterX();
//        LineMetrics labelMetrics = font.getLineMetrics(labelString, fontRenderContext);
//
//        int x = xCoordinate(pos) - labelHalfWidth;
//        int y = yCoordinateOfAxis() + majorTickHeightBelow + labelSep + (int) labelMetrics.getAscent();
//        if (labelBackgroundColor != null) {
//            graf.setColor(labelBackgroundColor);
//            graf.fillRect(x, yCoordinateOfAxis() + majorTickHeightBelow + labelSep, labelHalfWidth * 2, (int) labelMetrics.getHeight());
//        }
//
//        graf.setColor(labelColor);
//        graf.drawString(labelString, x, y);
//
//        graf.setColor(previousColor);
//    }
//
//    private void drawLabelIndirectly(int pos) {
//        FontRenderContext fontRenderContext = labelGraf.getFontRenderContext();
//        Font font = labelGraf.getFont();
//        String labelString = String.valueOf(pos);
//
//        /*
//         * Note: getStringBounds() is not generally guaranteed to give a rectangle
//         * that visually encloses all the rendered text, but in this case (printing
//         * numerals) it is reasonable to suppose that it will.
//         */
//        Rectangle2D labelBounds = font.getStringBounds(labelString, fontRenderContext);
//        LineMetrics labelMetrics = font.getLineMetrics(labelString, fontRenderContext);
//
//        int w = (int) labelBounds.getWidth();
//        int h = (int) labelBounds.getHeight();
//        if (labelBackgroundColor == null) {
//            labelGraf.setColor(TRANSPARENT);
//        } else {
//            labelGraf.setColor(labelBackgroundColor);
//        }
//        labelGraf.fillRect(0, 0, w, h);
//
//        labelGraf.setColor(labelColor);
//        labelGraf.drawString(labelString, 0, labelMetrics.getAscent());
//
//        int[] labelData = labelBuffer.getRGB(0, 0, w, h, null, 0, w);
//        int x = xCoordinate(pos) - (int) labelBounds.getCenterX();
//        int y = yCoordinateOfAxis() + majorTickHeightBelow + labelSep;
//
//        /*
//         * Deal with the case where the label only partially
//         * intersects the image, at the left or right edge
//         * of a tile.
//         */
//        int offset = 0;
//        int destWidth = w;
//        if (x < 0) {
//            offset = -x;
//            destWidth += x;
//            x = 0;
//        } else {
//            if (x+w > getWidth()) {
//                destWidth = getWidth() - x;
//                x = getWidth() - destWidth;
//            }
//        }
//        if (y + h > image.getHeight()) {
//            h = image.getHeight() - y;
//        }
//        // Formula for looking up a (x,y) coordinate
//        // pixel   = rgbArray[offset + (y-startY)*scansize + (x-startX)];
//        try {
//            image.setRGB(x, y, destWidth, h, labelData, offset, w);
//        }
//        catch (NullPointerException exp) {
//            // Passing in invalid coordinates - which ones
//            logger.error(String.format(
//                    "NullPointerException x='%d' y='%d' destWidth='%d' h='%d' labelData.size='%d' offset='%d' width='%d'",
//                    x, y, destWidth, h, labelData.length, offset, w));
//            exp.printStackTrace();
//        }
//        catch (RuntimeException exp) {
//            // Passing in invalid coordinates - which ones
//            logger.error(String.format(
//                    "%s x='%d' y='%d' destWidth='%d' h='%d' labelData.size='%d' offset='%d' width='%d'",
//                    exp.getMessage(), x, y, destWidth, h, labelData.length, offset, w));
//            exp.printStackTrace();
//        }
//    }
//
//    /**
//     * Draw a scale tick.
//     * @param pos the position of the tick, in interbase coordinates.
//     * @param tickHeightAbove the height of the tick above the axis, in pixels
//     * @param tickHeightBelow the height of the tick below the axis, in pixels
//     * @return a boolean value indicating whether the tick has been drawn.
//     *          If the position falls into a gap, the tick will not be drawn
//     *          and this method will return <code>false</code>.
//     */
//    private boolean drawScaleTick(int pos, int tickHeightAbove, int tickHeightBelow) {
//
//        if (gapContainingLocation(pos) != null)
//            return false;
//
//        int topOfTick    = yCoordinateOfAxis() - tickHeightAbove;
//        int bottomOfTick = yCoordinateOfAxis() + tickHeightBelow;
//
//        int x = xCoordinate(pos);
//        graf.drawLine(x, topOfTick, x, bottomOfTick);
//        return true;
//    }
//
//    protected int getAxisBreakGap() {
//        return axisBreakGap;
//    }
//
//    protected Color getLabelBackgroundColor() {
//        return labelBackgroundColor;
//    }
//    protected void setLabelBackgroundColor(Color labelBackgroundColor) {
//        this.labelBackgroundColor = labelBackgroundColor;
//    }
//
//    protected void setMargins(int leftMargin, int rightMargin) {
//        this.leftMargin  = leftMargin;
//        this.rightMargin = rightMargin;
//    }
//}