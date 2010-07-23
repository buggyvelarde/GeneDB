package org.genedb.web.gui;

import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;
import org.genedb.db.domain.objects.PolypeptideRegion;

import org.gmod.schema.feature.CytoplasmicRegion;
import org.gmod.schema.feature.MembraneStructure;
import org.gmod.schema.feature.MembraneStructureComponent;
import org.gmod.schema.feature.NonCytoplasmicRegion;
import org.gmod.schema.feature.TransmembraneRegion;

import org.apache.log4j.Logger;
import org.postgresql.util.MD5Digest;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.IndexColorModel;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public class RenderedProteinMap extends RenderedDiagram {
    private static final Logger logger = Logger.getLogger(RenderedProteinMap.class);

    private int hitHeight = 4;
    private int membraneStructureTrackHeight = 12;
    private int membraneStructureTrackGap = 4;
    private int membraneStructureMargin = 2;
    private int cytoplasmicRegionHeight = 2;
    private Color compoundFeatureBackgroundColor = new Color(200, 200, 200);
    private String transcriptName;

    private boolean hasMembraneStructure;

    public RenderedProteinMap(ProteinMapDiagram diagram) {
        super(diagram);
        setMaxWidth(800, getAxisBreakGap());
        setTickDistancesInPixels(10);
        setTrackHeight(8);
        colorModel = ColorModel.DIRECT;
        setMargins(10,100);
        setMinorTickHeightAbove(0);
        setMajorTickHeightAbove(0);
        setScaleVerticalPos(0);
        hasMembraneStructure = (getDiagram().getMembraneStructure() != null);
        transcriptName = diagram.getTranscriptUniqueName();
    }

    private void setTickDistancesInPixels(int pixels) {
        int majorTicksEvery = 5; // must divide 10

        int minorTickDistance = (int) Math.round(pixels * basesPerPixel);
        logger.debug(String.format("Ideal minor tick distance = %d", minorTickDistance));

        setTickDistances(minorTickDistance * majorTicksEvery, minorTickDistance);
    }

    @Override
    public int getHeight() {
        if (hasMembraneStructure) {
            return super.getHeight() + membraneStructureTrackHeight + membraneStructureTrackGap;
        }
        return super.getHeight();
    }

    @Override
    protected int topOfTrack(int trackNumber) {
        if (!hasMembraneStructure || trackNumber != 0) {
            return super.topOfTrack(trackNumber);
        }
        return membraneStructureTrackHeight + membraneStructureTrackGap + super.topOfTrack(trackNumber);
    }

    private int topOfMembraneStructureTrack() {
        if (!hasMembraneStructure) {
            throw new IllegalStateException("This diagram has no membrane structure");
        }
        return topOfTrack(0) - membraneStructureTrackHeight;
    }

    public String getPreferredFilename() {
        return transcriptName + "." + FILE_EXT;
    }

//    @Override
//    public String getRelativeRenderDirectory() {
//        return getDiagram().getOrganism();
//    }

    @Override
    public ProteinMapDiagram getDiagram() {
        return (ProteinMapDiagram) super.getDiagram();
    }

    @Override
    protected IndexColorModel byteIndexedColorModel() {
        throw new IllegalStateException("RenderedProteinMap does not support indexed colour");
    }

    @Override
    protected void renderFeatures() {
        renderedFeatures = new ArrayList<RenderedFeature>();
        super.renderFeatures();
        if (hasMembraneStructure) {
            renderMembraneStructure();
        }
        renderLabels();
    }

    private void renderMembraneStructure() {
        MembraneStructure membraneStructure = getDiagram().getMembraneStructure();
        logger.debug(String.format("Rendering membrane structure '%s' (id=%d)", membraneStructure.getUniqueName(),
            membraneStructure.getFeatureId()));

        graf.setColor(Color.BLUE);
        for (MembraneStructureComponent component: membraneStructure.getComponents()) {
            if (component instanceof CytoplasmicRegion) {
                drawCytoplasmicRegion((CytoplasmicRegion) component);
            }
            else if (component instanceof NonCytoplasmicRegion) {
                drawNonCytoplasmicRegion((NonCytoplasmicRegion) component);
            }
            else if (component instanceof TransmembraneRegion) {
                drawTransmembraneRegion((TransmembraneRegion) component);
            }
            else {
                throw new IllegalStateException(String.format("Unknown membrane structure component (%s)",
                    component.getClass()));
            }
        }
    }

    private void rect(String description, int x, int y, int w, int h) {
        rect(description, null, x, y, w, h);
    }
    private void rect(String description, String url, int x, int y, int w, int h) {
        graf.fillRect(x, y, w, h);
        addRenderedFeature(description, url, x, y, w, h);
    }

    private void drawCytoplasmicRegion(CytoplasmicRegion region) {
        logger.debug("Drawing cytoplasmic region");
        rect(String.format("Inside: %d - %d", 1 + region.getFmin(), region.getFmax()),
            xCoordinate(region.getFmin()),
            topOfMembraneStructureTrack() + membraneStructureTrackHeight - cytoplasmicRegionHeight - membraneStructureMargin,
            pixelWidth(region), cytoplasmicRegionHeight);
    }

    private void drawNonCytoplasmicRegion(NonCytoplasmicRegion region) {
        logger.debug("Drawing non-cytoplasmic region");
        rect(String.format("Outside: %d - %d", 1 + region.getFmin(), region.getFmax()),
            xCoordinate(region.getFmin()), topOfMembraneStructureTrack() + membraneStructureMargin,
            pixelWidth(region), cytoplasmicRegionHeight);
    }

    private void drawTransmembraneRegion(TransmembraneRegion region) {
        logger.debug("Drawing transmembrane region");
        rect(String.format("Transmembrane: %d - %d", 1 + region.getFmin(), region.getFmax()),
            xCoordinate(region.getFmin()), topOfMembraneStructureTrack() + membraneStructureMargin,
            pixelWidth(region), membraneStructureTrackHeight - 2 * membraneStructureMargin);
    }

    private Map<Integer, NavigableSet<Integer>> leftEdgesByTrack = new HashMap<Integer, NavigableSet<Integer>>();
    private void registerLeftEdge(int track, int x) {
        if (!leftEdgesByTrack.containsKey(track)) {
            leftEdgesByTrack.put(track, new TreeSet<Integer>());
        }
        leftEdgesByTrack.get(track).add(x);
    }

    @Override
    public String getKey() {
        return getKeyForTile(1, getStart(), getEnd() - getStart());
    }


    @Override
    public String getKeyForTile(int index, int start, int width) {
        return String.format("%d^^%s:%s:%d:%09d-%09ds%.0f.%s",
                getDiagram().getTranscriptFeatureId(),
                thumbNailMode ? "thumbnailp": "protein",
                getDiagram().getOrganism(),
                index,
                start,
                width,
                getBasesPerPixel(),
                FILE_EXT);
    }

    private class Label {
        private String shortLabel;
        private String longLabel;
        private String url;
        private int trackBelowLabel;
        private int x;
        private String uniqueName;

        public Label(String shortLabel, String longLabel, int trackBelowLabel, int x, String url) {
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
            this.trackBelowLabel = trackBelowLabel;
            this.x = x;
            this.url = url;
        }

        public void draw() {
            int y = topOfTrack(trackBelowLabel + 1) + 4;

            graf.setColor(Color.BLACK);
            if (fits(longLabel)) {
                drawString(longLabel, x, y, longLabel, url);
                registerLeftEdge(trackBelowLabel + 1, x);
                registerLeftEdge(trackBelowLabel + 2, x);
            }
            else if (fits(shortLabel)) {
                drawString(shortLabel, x, y, longLabel, url);
                registerLeftEdge(trackBelowLabel + 1, x);
                registerLeftEdge(trackBelowLabel + 2, x);
            }
            else {
                logger.info(String.format("No room to draw label '%s', nor even the short form '%s'", longLabel, shortLabel));
            }
        }

        private void drawString(String string, int x, int y, String description, String url) {
            graf.drawString(string, x, y);
            Rectangle2D stringBounds = graf.getFontMetrics().getStringBounds(string, graf);
            addRenderedFeature(description, url, x + (int) stringBounds.getMinX(), y + (int) stringBounds.getMinY(),
                (int) stringBounds.getWidth(), (int) stringBounds.getHeight());
        }

        private <T> void addAllIfNotNull(Collection<T> collection, Collection<T> stuffToAdd) {
            if (stuffToAdd != null) {
                collection.addAll(stuffToAdd);
            }
        }

        private boolean fits(String label) {
            NavigableSet<Integer> leftEdges = new TreeSet<Integer>();
            addAllIfNotNull(leftEdges, leftEdgesByTrack.get(trackBelowLabel + 1));
            addAllIfNotNull(leftEdges, leftEdgesByTrack.get(trackBelowLabel + 2));

            if (leftEdges.ceiling(x) == null) {
                return true;
            }

            int availableWidth = leftEdges.ceiling(x) - x;
            Rectangle2D stringBounds = graf.getFontMetrics().getStringBounds(label, graf);
            return stringBounds.getWidth() <= availableWidth;
        }
    }

    private LinkedList<Label> labelQueue = new LinkedList<Label>();
    private void queueLabel(String shortLabel, String longLabel, int trackBelowLabel, int x, String url) {
        labelQueue.addFirst(new Label(shortLabel, longLabel, trackBelowLabel, x, url));
    }

    private void renderLabels() {
        for (Label label: labelQueue) {
            label.draw();
        }
    }

    @Override
    protected void drawCompoundFeature(AllocatedCompoundFeature allocatedCompoundFeature) {
        int track = allocatedCompoundFeature.getTrack();
        CompoundLocatedFeature feature = allocatedCompoundFeature.getFeature();
        int numSubtracks = allocatedCompoundFeature.getSubtracks().size();
        int topTrack = track + numSubtracks - 1;
        int height = getTrackHeight() * numSubtracks;

        String shortLabel = feature.getShortName();
        String longLabel  = feature.getName();
        if (feature.getDescription() != null) {
            longLabel += " " + feature.getDescription();
        }

        int x = xCoordinate(feature.getFmin()) - 2;
        int y = topOfTrack(topTrack);
        graf.setColor(compoundFeatureBackgroundColor);
        graf.fillRect(x, y, pixelWidth(feature) + 4, height);

        for (int t = track; t <= topTrack; t++) {
            registerLeftEdge(t, x);
        }

        queueLabel(shortLabel, longLabel, topTrack, x, feature.getUrl());
    }

    @Override
    protected void drawFeature(int track, LocatedFeature subfeature, AllocatedCompoundFeature superfeature) {

        graf.setColor(subfeature.getColor() == null ? Color.red : subfeature.getColor());

        String title = String.format("%s (%d - %d)", subfeature.getUniqueName(), 1 + subfeature.getFmin(), subfeature.getFmax());
        if (subfeature instanceof PolypeptideRegion) {
            PolypeptideRegion polypeptideRegion = (PolypeptideRegion) subfeature;
            if (polypeptideRegion.getDescription() != null) {
                title = String.format("%s %s (%d - %d)", subfeature.getUniqueName(), polypeptideRegion.getDescription(), 1 + subfeature.getFmin(), subfeature.getFmax());
            }
        }

        rect(title,
            subfeature.getUrl(), xCoordinate(subfeature.getFmin()), topOfHit(track), pixelWidth(subfeature), getHitHeight());
    }

    private int topOfHit(int track) {
        return topOfTrack(track) + (getTrackHeight() - getHitHeight()) / 2;
    }
    private int getHitHeight() {
        return hitHeight;
    }

    private List<RenderedFeature> renderedFeatures;

    /**
     * Once the diagram has been rendered, this method will return the HTML
     * code for a &lt;map&gt; element describing the rendered features.
     *
     * @return
     */
    public String getRenderedFeaturesAsHTML(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<map name='%s' id='%1$s'>\n", name));
        for (RenderedFeature renderedFeature: renderedFeatures) {
            if (renderedFeature.url == null) {
                sb.append(String.format(" <area shape='rect' coords='%d,%d,%d,%d' title='%s' nohref='true' />\n",
                    renderedFeature.x, renderedFeature.y,
                    renderedFeature.x + renderedFeature.w, renderedFeature.y + renderedFeature.h,
                    renderedFeature.description));
            }
            else {
                sb.append(String.format(" <area shape='rect' coords='%d,%d,%d,%d' title=\"%s\" href='%s' />\n",
                    renderedFeature.x, renderedFeature.y,
                    renderedFeature.x + renderedFeature.w, renderedFeature.y + renderedFeature.h,
                    renderedFeature.description.replaceAll("\"", "&quot;"), renderedFeature.url));
            }
        }
        sb.append("</map>\n");
        return sb.toString();
    }

    private void addRenderedFeature(String description, String url, int x, int y, int w, int h) {
        renderedFeatures.add(new RenderedFeature(description, url, x, y, w, h));
    }

    private class RenderedFeature {
        private String description, url;
        private int x, y, w, h;

        public RenderedFeature(String description, String url, int x, int y, int w, int h) {
            this.description = description;
            this.url = url;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
