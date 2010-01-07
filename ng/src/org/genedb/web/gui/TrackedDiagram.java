package org.genedb.web.gui;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class TrackedDiagram {

    private static final Logger logger = Logger.getLogger(TrackedDiagram.class);

    protected AllocatedCompoundFeature.Mode packSubfeatures = AllocatedCompoundFeature.Mode.UNPACKED;

    /**
     * The number of blank tracks to leave above (or,
     * for negative tracks, below) each compound feature.
     */
    protected int numberOfBlankTracksAboveCompoundFeature = 0;

    private int start, end;
    private int numberOfPositiveTracks = 0, numberOfNegativeTracks = 0;

    public int numberOfPositiveTracks() {
        return numberOfPositiveTracks;
    }

    public int numberOfNegativeTracks() {
        return numberOfNegativeTracks;
    }

    public int numberOfTracks() {
        return numberOfPositiveTracks() + numberOfNegativeTracks();
    }

    private SortedSet<AllocatedCompoundFeature> allocatedCompoundFeatures = new TreeSet<AllocatedCompoundFeature>();
    private List<LocatedFeature> globalFeatures = new ArrayList<LocatedFeature>();

    public SortedSet<AllocatedCompoundFeature> getAllocatedCompoundFeatures() {
        return allocatedCompoundFeatures;
    }

    /**
     * Get the global features of this diagram.
     * @return a list of the global features, in left-to-right order.
     */
    public List<LocatedFeature> getGlobalFeatures() {
        return globalFeatures;
    }

    /**
     * Add a compound feature to this diagram
     * @param feature the feature to add
     * @param track the track number
     */
    private void addFeature(AllocatedCompoundFeature feature, int track) {
        feature.setTrack(track);
        allocatedCompoundFeatures.add(feature);

        int numberOfSubtracks = feature.getSubtracks().size();
        if (track + numberOfSubtracks - 1 + numberOfBlankTracksAboveCompoundFeature > numberOfPositiveTracks) {
            numberOfPositiveTracks = track + numberOfSubtracks - 1 + numberOfBlankTracksAboveCompoundFeature;
        } else if (-track + numberOfSubtracks - 1 + numberOfBlankTracksAboveCompoundFeature > numberOfNegativeTracks) {
            numberOfNegativeTracks = -track + numberOfSubtracks - 1 + numberOfBlankTracksAboveCompoundFeature;
        }
    }

    /**
     * Add a global feature. A global feature does not belong to a track,
     * it's a feature of an entire region of the diagram. The obvious example
     * is a gap in a sequence.
     *
     * Global features should be added in left-to-right order.
     */
    protected void addGlobalFeature(LocatedFeature feature) {
        globalFeatures.add(feature);
    }

    /**
     * The track number should be between -1 and
     * <code>-numberOfNegativeTracks()</code> inclusive, to retrieve a
     * negative track; or between 1 and <code>numberOfPositiveTracks()</code>
     * inclusive, to retrieve a negative track.
     *
     * @param track
     * @return a list of Transcripts, or null if there are none
     */
    @Deprecated
    public List<LocatedFeature> getTrack(int track) {
        List<LocatedFeature> ret = new ArrayList<LocatedFeature>();

        if (track == 0)
            return getGlobalFeatures();

        if (track > numberOfPositiveTracks() || track < -numberOfNegativeTracks())
            return null;

        for (AllocatedCompoundFeature f: allocatedCompoundFeatures) {
            ret.addAll(f.forTrack(track));
        }

        return ret;
    }

    public TrackedDiagram(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Get the location (interbase coordinates) of the start of the diagram.
     *
     * @return the start location
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the location (interbase coordinates) of the end of the diagram.
     *
     * @return the end location
     */
    public int getEnd() {
        return end;
    }

    /**
     * Get the size of the diagram in bases, equivalent to
     * <code>(getEnd() - getStart())</code>.
     *
     * @return the size of the diagram
     */
    public int getSize() {
        return end - start;
    }

    /**
     * Allocates each transcript to a track in the appropriate way, grouping
     * together the different subfeatures of each compound feature.
     *
     * @param boundaries
     * @param negativeHalf <code>true</code> for the negative
     * @return the diagram Half
     */
    protected void allocateTracks(Iterable<? extends CompoundLocatedFeature> compoundFeatures, boolean negativeHalf) {
        DiagramLayout layout = new DiagramLayout();

        for (CompoundLocatedFeature compoundFeature : compoundFeatures) {
            AllocatedCompoundFeature allocatedCompoundFeature
                = new AllocatedCompoundFeature(compoundFeature, 0, packSubfeatures);
            int numSubtracks = allocatedCompoundFeature.getSubtracks().size();
            if (numSubtracks == 0) {
                /*
                 * Give a more comprehensible error message if this is a gene.
                 */
                if (compoundFeature instanceof BasicGene)
                    logger.error(String.format("The gene '%s' has no transcripts!", compoundFeature
                        .getUniqueName()));
                else
                    logger.error(String.format("The compound feature '%s' has no subfeatures!", compoundFeature
                            .getUniqueName()));
                continue;
            }

            int track = layout.addBlock(compoundFeature.getFmin(), compoundFeature.getFmax(),
                numSubtracks + numberOfBlankTracksAboveCompoundFeature);
            addFeature(allocatedCompoundFeature, negativeHalf ? -track : track);
        }
    }

}