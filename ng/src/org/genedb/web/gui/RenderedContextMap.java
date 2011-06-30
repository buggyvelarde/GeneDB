//package org.genedb.web.gui;
//
//import net.sf.json.JSONString;
//
//import org.genedb.db.domain.objects.CompoundLocatedFeature;
//import org.genedb.db.domain.objects.Exon;
//import org.genedb.db.domain.objects.Gap;
//import org.genedb.db.domain.objects.LocatedFeature;
//import org.genedb.db.domain.objects.Transcript;
//import org.genedb.db.domain.objects.TranscriptComponent;
//
//import org.apache.log4j.Logger;
//
//import java.awt.Color;
//import java.awt.image.IndexColorModel;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Renders a {@link ContextMapDiagram} as an image.
// *
// * The rendered diagram consists of a number of gene tracks (positive above and
// * negative below). In the centre is a scale track, which shows the scale of the
// * diagram.
// *<p>
// * Each exon is represented by an unbordered rectangle of the appropriate
// * colour, centred vertically within the gene track. An intron is represented by
// * a narrower rectangle of the same colour, also vertically centred and
// * continuous with the exons it separates.
// *<p>
// * If the rendered diagram is more than 32767 pixels wide, the image will still
// * be correctly generated but most decoding software will fail in one of several
// * amusing ways. Therefore it is advisable to use a ContextMapDiagram
// * representing fewer than 327670 bases (assuming the current default scale of
// * 10 bases per pixel). Moreover, a rendered diagram even 10k pixels wide
// * appears to crash at least some versions of Firefox on Linux, so we are
// * currently limiting ourselves to tiles 5000 pixels wide.
// *
// * @author rh11
// */
//public class RenderedContextMap extends RenderedDiagram {
//    static final Logger logger = Logger.getLogger(RenderedContextMap.class);
//
//    /**
//     * The height of the rectangle representing an exon
//     */
//    private int exonRectHeight = 12;
//    /**
//     * The height of the rectangle representing an intron
//     */
//    private int intronRectHeight = 2;
//
//    /**
//     * The height of the rectangle representing an untranslated region
//     */
//    private int utrRectHeight = 6;
//
//    public RenderedContextMap(ContextMapDiagram diagram) {
//
//        super(diagram);
//        setLabelBackgroundColor(new Color(0xF0, 0xF0, 0xE4));
//
//        for (LocatedFeature globalFeature: diagram.getGlobalFeatures()) {
//            if (globalFeature instanceof Gap) {
//                addGap((Gap) globalFeature);
//            }
//        }
//        calculateSizeExcludingGaps();
//    }
//
//    @Override
//    public ContextMapDiagram getDiagram() {
//        return (ContextMapDiagram) super.getDiagram();
//    }
//
//
//    /**
//     * Get the height of the rectangles used to represent exons in this diagram.
//     * @return the height in pixels
//     */
//    public int getExonRectHeight() {
//        return exonRectHeight;
//    }
//
//    /**
//     * Set the height of the rectangles used to represent exons in this diagram
//     * @param exonRectHeight the height in pixels
//     */
//    public void setExonRectHeight(int exonRectHeight) {
//        this.exonRectHeight = exonRectHeight;
//    }
//
//    /**
//     * Get the height of the rectangles used to represent introns in this diagram.
//     * @return the height in pixels
//     */
//   public int getIntronRectHeight() {
//        return intronRectHeight;
//    }
//
//   /**
//    * Set the height of the rectangles used to represent introns in this diagram
//    * @param intronRectHeight the height in pixels
//    */
//    public void setIntronRectHeight(int intronRectHeight) {
//        this.intronRectHeight = intronRectHeight;
//    }
//
//    /**
//     * Get the height of the rectangles used to represent untranslated regions in this diagram.
//     * @return the height in pixels
//     */
//    public int getUTRRectHeight() {
//        return utrRectHeight;
//    }
//
//    /**
//     * Set the height of the rectangles used to represent untranslated regions in this diagram
//     * @param utrRectHeight the height in pixels
//     */
//    public void setUTRRectHeight(int utrRectHeight) {
//        this.utrRectHeight = utrRectHeight;
//    }
//
//    @Override
//    public RenderedContextMap asThumbnail(int maxWidth) {
//        super.asThumbnail(maxWidth);
//        setExonRectHeight(2);
//        setIntronRectHeight(2);
//        setUTRRectHeight(2);
//        setTickDistances(0, 0);
//        setScaleColor(Color.GRAY);
//        setBoundaryTickHeight(0);
//        return this;
//    }
//
//    @Override
//    protected void drawFeature(int track, LocatedFeature subfeature, AllocatedCompoundFeature superfeature) {
//        if (!(subfeature instanceof Transcript)) {
//            logger.error(String.format("Located feature '%s' is a '%s' not a Transcript", superfeature, superfeature.getClass()));
//            return;
//        }
//
//        renderedFeatures.add(new RenderedFeature(superfeature.getFeature(), subfeature, track, ((Transcript) subfeature).getProducts()));
//
//        drawTranscript(track, (Transcript) subfeature);
//    }
//
//    private void drawTranscript(int trackNumber, Transcript transcript) {
//        logger.debug(String.format("Drawing transcript %s (%d..%d) on track %d", transcript
//                .getUniqueName(), transcript.getFmin(), transcript.getFmax(), trackNumber));
//
//        Color color = ArtemisColours.getColour(transcript.getColourId());
//        graf.setColor(color);
//
//        int currentPos = transcript.getFmin();
//        for (TranscriptComponent component : transcript.getComponents()) {
//            drawIntron(trackNumber, currentPos, component.getStart());
//            drawComponent(trackNumber, component);
//            currentPos = component.getEnd();
//        }
//        drawIntron(trackNumber, currentPos, transcript.getFmax());
//    }
//
//    private void drawComponent(int trackNumber, TranscriptComponent component) {
//        int start = component.getStart(), end = component.getEnd();
//        if (end <= start) {
//            logger.warn("Drawing nothing: empty or negative");
//            return;
//        }
//
//        int x = xCoordinate(component.getStart());
//        int width = pixelWidth(component);
//
//        if (component instanceof Exon) {
//            drawExon(trackNumber, x, width);
//        } else {
//            drawUTR(trackNumber, x, width);
//        }
//    }
//
//    /**
//     * Calculate the width in pixels of a transcript component.
//     *
//     * @param transcriptComponent the transcript component
//     * @return the width in pixels
//     */
//    private int pixelWidth(TranscriptComponent transcriptComponent) {
//        return pixelWidth(transcriptComponent.getStart(), transcriptComponent.getEnd());
//    }
//
//    private void drawIntron(int trackNumber, int start, int end) {
//        if (end <= start) {
//            return;
//        }
//        int x = xCoordinate(start), y = topOfIntron(trackNumber);
//        int width = pixelWidth(start, end);
//        graf.fillRect(x, y, width, intronRectHeight);
//    }
//
//    private void drawExon(int trackNumber, int x, int width) {
//        /*
//         * Always draw exons at least 1 pixel wide, otherwise it
//         * looks confusing. See the P. falciparum gene PF14_0177
//         * for example.
//         */
//        if (width < 1)
//            width = 1;
//
//        int y = topOfExon(trackNumber);
//        graf.fillRect(x, y, width, exonRectHeight);
//    }
//
//    private void drawUTR(int trackNumber, int x, int width) {
//        int y = topOfUTR(trackNumber);
//        graf.fillRect(x, y, width, utrRectHeight);
//   }
//
//
//    private int topOfIntron(int trackNumber) {
//        return topOfTrack(trackNumber) + (getTrackHeight() - intronRectHeight) / 2;
//    }
//
//    private int topOfExon(int trackNumber) {
//        return topOfTrack(trackNumber) + (getTrackHeight() - exonRectHeight) / 2;
//    }
//
//    private int topOfUTR(int trackNumber) {
//        return topOfTrack(trackNumber) + (getTrackHeight() - utrRectHeight) / 2;
//    }
//
//    private List<String> products = new ArrayList<String>();
//    private Map<String,Integer> productIndexByName = new HashMap<String,Integer>();
//
//    private List<RenderedFeature> renderedFeatures;
//    private int productIndex(String productName) {
//        if (productIndexByName.containsKey(productName))
//            return productIndexByName.get(productName);
//        else {
//            products.add(productName);
//            int indexOfProduct = products.size() - 1;
//            productIndexByName.put(productName, indexOfProduct);
//            return indexOfProduct;
//        }
//    }
//
//    public List<String> getProducts() {
//        return products;
//    }
//
//    /**
//     * Represents a located feature that has been rendered,
//     * and stores its position on the rendered diagram.
//     *
//     * @author rh11
//     */
//    public class RenderedFeature implements JSONString {
//        private String uniqueName, compoundUniqueName = "", compoundName = "";
//        private int track, pixelX, pixelWidth;
//        private List<Integer> productIndices = new ArrayList<Integer>();
//        public RenderedFeature (CompoundLocatedFeature compoundFeature, LocatedFeature subfeature, int track, List<String> productNames) {
//            this(subfeature.getUniqueName(), compoundFeature.getUniqueName(),
//            compoundFeature.getName(), track,
//            xCoordinate(subfeature.getFmin()), pixelWidth(subfeature));
//
//            if (productNames != null) {
//                for (String productName: productNames)
//                    productIndices.add(productIndex(productName));
//            }
//        }
//        public RenderedFeature(String uniqueName, int track, int pixelX, int pixelWidth) {
//            this.uniqueName = uniqueName;
//            this.track = track;
//            this.pixelX = pixelX;
//            this.pixelWidth = pixelWidth;
//        }
//        private RenderedFeature(String uniqueName, String compoundUniqueName, String compoundName, int track, int pixelX, int pixelWidth) {
//            this(uniqueName, track, pixelX, pixelWidth);
//
//            if (compoundUniqueName != null)
//                this.compoundUniqueName = compoundUniqueName;
//
//            if (compoundName != null)
//                this.compoundName = compoundName;
//        }
//        private String getProductIndicesAsJSONString() {
//            StringBuilder sb = new StringBuilder();
//            boolean first = true;
//            for (int productIndex: productIndices) {
//                if (first)
//                    first = false;
//                else
//                    sb.append(',');
//                sb.append(productIndex);
//            }
//            return "[" + sb.toString() + "]";
//        }
//        public String toJSONString() {
//            return String.format("[\"%s\",\"%s\",\"%s\",%d,%d,%d,%s]",
//                uniqueName, compoundUniqueName, compoundName, track, pixelX, pixelWidth, getProductIndicesAsJSONString());
//        }
//    }
//
//    @Override
//    protected void beforeRender() throws ImageCreationException {
//        super.beforeRender();
//
//        renderedFeatures = new ArrayList<RenderedFeature>();
//
//        for (Gap gap: getGaps()) {
//            renderedFeatures.add(new RenderedFeature(
//                String.format("gap: %d-%d", gap.getFmin()+1, gap.getFmax()),
//                /*track*/ 0, xCoordinate(gap.getFmin()), getAxisBreakGap()));
//        }
//    }
//
//    @Override
//    protected IndexColorModel byteIndexedColorModel() {
//        return ArtemisColours.colorModel(getLabelBackgroundColor());
//    }
//
//    @Override
//    public String getKey() {
//        return getKeyForTile(1, getStart(), getEnd() - getStart());
//    }
//
//    @Override
//    public String getKeyForTile(int index, int start, int width) {
//        return String.format("%s^^%s:%s:%d:%09d-%09ds%.0f.%s",
//                getDiagram().getChromosomeFeatureId(),
//                thumbNailMode ? "thumbnail": "context",
//                getDiagram().getOrganism(),
//                index,
//                start,
//                width,
//                getBasesPerPixel(),
//                FILE_EXT);
//    }
//
//    /**
//     * Once the diagram has been rendered, this method will return a list of
//     * all the rendered features with their locations on the rendered image.
//     *
//     * @return
//     */
//    public List<RenderedFeature> getRenderedFeatures() {
//        return renderedFeatures;
//    }
//
//}
