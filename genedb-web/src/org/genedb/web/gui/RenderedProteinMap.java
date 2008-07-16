package org.genedb.web.gui;

import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;
import org.genedb.web.gui.RenderedContextMap.RenderedFeature;
import org.genedb.web.gui.TrackedDiagram.AllocatedCompoundFeature;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.List;

public class RenderedProteinMap extends RenderedDiagram {
    private static final Logger logger = Logger.getLogger(RenderedProteinMap.class);

    private int hitHeight = 4;

    protected List<RenderedFeature> renderedFeatures;

    public RenderedProteinMap(ProteinMapDiagram diagram) {
        super(diagram);
        setMaxWidth(800, getAxisBreakGap());
        setTickDistancesInPixels(10);
        setTrackHeight(8);
        colorModel = ColorModel.DIRECT;
        setMargins(10,20);
        setMinorTickHeightAbove(0);
        setMajorTickHeightAbove(0);
    }

    private void setTickDistancesInPixels(int pixels) {
        int majorTicksEvery = 5; // must divide 10

        int minorTickDistance = (int) Math.round(pixels * basesPerPixel);
        logger.debug(String.format("Ideal minor tick distance = %d", minorTickDistance));

        setTickDistances(minorTickDistance * majorTicksEvery, minorTickDistance);
        /*
        int majorTickDistance = (int) Math.round(Math.pow(10, Math.round(Math.log10(majorTicksEvery * minorTickDistance))));
        if (majorTickDistance < 10) {
            majorTickDistance = 10;
        }

        logger.debug(String.format("Setting major tick distance to %d", majorTickDistance));
        this.setTickDistances(majorTickDistance, majorTickDistance / majorTicksEvery);
        */
    }

    @Override
    public String getRelativeRenderDirectory() {
        return getDiagram().getOrganism();
    }

    @Override
    public ProteinMapDiagram getDiagram() {
        return (ProteinMapDiagram) super.getDiagram();
    }

    @Override
    protected IndexColorModel byteIndexedColorModel() {
        throw new IllegalStateException();
    }

    @Override
    protected void drawCompoundFeature(AllocatedCompoundFeature allocatedCompoundFeature) {
        int track = allocatedCompoundFeature.getTrack();
        CompoundLocatedFeature feature = allocatedCompoundFeature.getFeature();
        int numSubfeatures = feature.getSubfeatures().size();
        int topTrack = track + numSubfeatures - 1;
        int height = getTrackHeight() * numSubfeatures;

        graf.setColor(Color.LIGHT_GRAY);
        graf.fillRect(xCoordinate(feature.getFmin()) - 2, topOfTrack(topTrack), pixelWidth(feature) + 4, height);
    }

    @Override
    protected void drawFeature(int track, LocatedFeature subfeature,
            @SuppressWarnings("unused") AllocatedCompoundFeature superfeature) {

        graf.setColor(Color.red);
        graf.fillRect(xCoordinate(subfeature.getFmin()), topOfHit(track), pixelWidth(subfeature), getHitHeight());
    }

    private int topOfHit(int track) {
        return topOfTrack(track) + (getTrackHeight() - getHitHeight()) / 2;
    }
    private int getHitHeight() {
        return hitHeight;
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
}
