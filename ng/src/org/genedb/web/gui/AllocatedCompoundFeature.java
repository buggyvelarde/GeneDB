package org.genedb.web.gui;

import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a CompoundLocatedFeature that has been allocated a track
 * or tracks.
 */
public class AllocatedCompoundFeature implements Comparable<AllocatedCompoundFeature> {
    private static final Logger logger = Logger.getLogger(AllocatedCompoundFeature.class);

    public enum Mode {
        UNPACKED, PACKED, STRATIFIED, STRATIFIED_LTR
    }

    private final CompoundLocatedFeature feature;
    private int track;

    /**
     * Create an AllocatedCompoundFeature with the specified feature and track number.
     * Each subfeature is allocated its own track.
     *
     * @param feature the feature
     * @param track the number of the track
     */
    public AllocatedCompoundFeature(CompoundLocatedFeature feature, int track) {
        this(feature, track, Mode.UNPACKED);
    }

    /**
     * Create an AllocatedCompoundFeature with the specified feature and track number.
     *
     * @param feature the feature
     * @param track the number of the track
     * @param packSubfeatures whether to pack subfeatures into as few tracks as possible:
     *          if <code>false</code>, each subfeature is allocated its own track.
     */
    public AllocatedCompoundFeature(CompoundLocatedFeature feature, int track, Mode mode) {
        this.feature = feature;
        this.track = track;
        
        logger.debug(feature.getUniqueName());
        
        switch (mode) {
        case UNPACKED:       dontPackSubfeatures();         break;
        case PACKED:         packSubfeatures();             break;
        case STRATIFIED:     stratifySubfeatures();         break;
        case STRATIFIED_LTR: stratifySubfeaturesByFirst(); break;
        }
    }

    private List<Collection<LocatedFeature>> packedFeatures = new ArrayList<Collection<LocatedFeature>>();

    private void addSubfeature(int track, LocatedFeature subfeature) {
        for (int i = packedFeatures.size(); i <= track; i++) {
            packedFeatures.add(new ArrayList<LocatedFeature>());
        }
        packedFeatures.get(track).add(subfeature);
        logger.trace(String.format("Added '%s' to track %d", subfeature.getUniqueName(), track));
    }

    private void packSubfeatures() {
        DiagramLayout layout = new DiagramLayout();
        for (LocatedFeature subfeature: feature.getSubfeaturesOrderedByPosition()) {
            int track = layout.addBlock(subfeature.getFmin(), subfeature.getFmax(), 1) - 1;
            addSubfeature(track, subfeature);
        }
    }

    private void stratifySubfeatures() {
        stratifySubfeatures(feature.getStratifiedSubfeatures());
    }

    private void stratifySubfeaturesByFirst() {
        stratifySubfeatures(feature.getStratifiedSubfeaturesByFirst());
    }

    private void stratifySubfeatures(Iterable<? extends Iterable<? extends LocatedFeature>> strata) {
        int baseTrack = 0;
        for (Iterable<? extends LocatedFeature> stratum: strata) {
            logger.trace("Start of stratum");
            DiagramLayout layout = new DiagramLayout();
            for (LocatedFeature subfeature: stratum) {
                int track = layout.addBlock(subfeature.getFmin(), subfeature.getFmax(), 1) - 1;
                addSubfeature(track + baseTrack, subfeature);
            }
            baseTrack += layout.numberOfTracks();
        }
    }

    private void dontPackSubfeatures() {
        for (LocatedFeature subfeature: feature.getSubfeatures()) {
            packedFeatures.add(Collections.singleton(subfeature));
        }
    }

    /**
     * Get the sub-tracks of this feature. Each track consists of a
     * (non-empty) collection of <code>LocatedFeature</code>s.
     *
     * @return a list of tracks
     */
    public List<Collection<LocatedFeature>> getSubtracks() {
        return packedFeatures;
    }

    public CompoundLocatedFeature getFeature() {
        return feature;
    }

    /**
     * Get the track this compound feature starts on.
     * @return the track number: positive or negative, non-zero.
     */
    public int getTrack() {
        return track;
    }

    /**
     * Set the start track for this compound feature.
     * @param track
     */
    public void setTrack(int track) {
        if (track == 0) {
            throw new IllegalArgumentException("Track number must be positive or negative, not zero");
        }
        this.track = track;
    }

    /**
     * Get the subfeatures that sit on the specified track.
     *
     * @param subfeatureTrack the track number
     * @return a collection of the subfeatures on the specified track, possibly empty
     */
    Collection<? extends LocatedFeature> forTrack(int subfeatureTrack) {
        if (subfeatureTrack == 0)
            throw new IllegalArgumentException("Track number must be positive or negative, not zero");
        if ((track < 0 && subfeatureTrack > 0) || (track > 0 && subfeatureTrack < 0))
            return Collections.emptySet();
        int subfeatureIndex;
        if (track > 0)
            subfeatureIndex = subfeatureTrack - track;
        else
            subfeatureIndex = track - subfeatureTrack;

        if (subfeatureIndex < 0 || subfeatureIndex >= packedFeatures.size())
            return Collections.emptySet();

        return packedFeatures.get(subfeatureIndex);
    }

    public int compareTo (AllocatedCompoundFeature other) {
        return this.feature.compareTo(other.feature);
    }
}