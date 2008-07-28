package org.genedb.web.gui;

import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a CompoundLocatedFeature that has been allocated a track
 * or tracks.
 */
public class AllocatedCompoundFeature implements Comparable<AllocatedCompoundFeature> {
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
        this(feature, track, false);
    }

    /**
     * Create an AllocatedCompoundFeature with the specified feature and track number.
     *
     * @param feature the feature
     * @param track the number of the track
     * @param packSubfeatures whether to pack subfeatures into as few tracks as possible:
     *          if <code>false</code>, each subfeature is allocated its own track.
     */
    public AllocatedCompoundFeature(CompoundLocatedFeature feature, int track, boolean packSubfeatures) {
        this.feature = feature;
        this.track = track;

        if (packSubfeatures) {
            packSubfeatures();
        }
        else {
            dontPackSubfeatures();
        }
    }

    private List<Collection<LocatedFeature>> packedFeatures = new ArrayList<Collection<LocatedFeature>>();
    private void packSubfeatures() {
        BoundarySet<LocatedFeature> boundaries = new BoundarySet<LocatedFeature>(feature.getSubfeatures());
        BitSet inUse = new BitSet();
        Map<LocatedFeature,Integer> trackByFeature = new HashMap<LocatedFeature,Integer>();

        for (Boundary<LocatedFeature> boundary: boundaries) {
            switch (boundary.type) {
            case START:
                int track = inUse.nextClearBit(0);
                inUse.set(track);
                trackByFeature.put(boundary.feature, track);

                assert track <= packedFeatures.size();
                if (track == packedFeatures.size()) {
                    packedFeatures.add(new ArrayList<LocatedFeature>());
                }
                packedFeatures.get(track).add(boundary.feature);
                break;
            case END:
                inUse.clear(trackByFeature.get(boundary.feature));
                trackByFeature.remove(boundary.feature);
                break;
            }
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