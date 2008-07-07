package org.genedb.web.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.CompoundLocatedFeature;
import org.genedb.db.domain.objects.LocatedFeature;

public class TrackedDiagram {

    protected static final Logger logger = Logger.getLogger(TrackedDiagram.class);

    /**
     * Represents the boundary (either the START of the END) of a gene.
     */
    protected static class Boundary<T extends LocatedFeature>
        implements Comparable<Boundary<? extends LocatedFeature>>
    {
        /**
         * Compares Boundaries, ordering them in the appropriate way for
         * track-allocation. If two boundaries have different locations, the
         * leftmost one goes first; if their locations coincide then we put
         * START boundaries before ENDs, and we put the START of a longer feature
         * before the START of a shorter.
         */
        public int compareTo(Boundary<? extends LocatedFeature> other) {
            int thisLoc = this.getLocation();
            int otherLoc = other.getLocation();

            if (thisLoc != otherLoc)
                return (thisLoc - otherLoc);
            else if (this.type == Boundary.Type.START && other.type == Boundary.Type.END)
                return -1;
            else if (this.type == Boundary.Type.END && other.type == Boundary.Type.START)
                return +1;
            else if (this.type == Boundary.Type.START && other.type == Boundary.Type.START) {
                int ret = other.feature.getFmax() - this.feature.getFmax();
                if (ret != 0)
                    return ret;
            }
            return this.feature.getUniqueName().compareTo(other.feature.getUniqueName());
        }

        /**
         * Two Boundary objects are equal if they have the same type and
         * they refer to the same feature.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Boundary) {
                @SuppressWarnings("unchecked")
                Boundary<T> other = (Boundary<T>) obj;
                return (this.type == other.type && this.feature.equals(other.feature));
            }
            return false;
        }

        public enum Type {
            START, END
        }

        public Type type;
        public T feature;

        Boundary(Type type, T feature) {
            this.type = type;
            this.feature = feature;
        }

        /**
         * Returns the location of the boundary, which is to say the
         * <code>fmin</code> of the feature for a START boundary, or the
         * <code>fmax</code> for an END.
         *
         * @return the location
         */
        public int getLocation() {
            switch (type) {
            case START:
                return feature.getFmin();
            case END:
                return feature.getFmax();
            default:
                throw new IllegalStateException(
                        "GeneBoundary type is invalid. This should never happen!");
            }
        }

        @Override
        public String toString() {
            return String.format("%s of %s (%d..%d)", type, feature.getUniqueName(), feature.getFmin(),
                feature.getFmax());
        }
    }

    class BoundarySet<T extends CompoundLocatedFeature> extends TreeSet<Boundary<T>> {
        public BoundarySet (Collection<T> features) {
            super();
            addFeatures(features);
        }
        public void addFeatures(Collection<T> genes) {
            for (T gene: genes) {
                this.add(new Boundary<T>(Boundary.Type.START, gene));
                this.add(new Boundary<T>(Boundary.Type.END,   gene));
            }
        }
        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            boolean first = true;
            for (Boundary<? extends CompoundLocatedFeature> boundary: this) {
                if (first)
                    first = false;
                else
                    s.append(", ");
                s.append(boundary.toString());
            }
            return s.toString();
        }
    }

    /**
     * Represents a CompoundLocatedFeature that has been allocated a track
     * or tracks.
     */
    public class AllocatedCompoundFeature implements Comparable<AllocatedCompoundFeature> {
        private final CompoundLocatedFeature feature;
        private final int track;
        public AllocatedCompoundFeature(CompoundLocatedFeature feature, int track) {
            if (track == 0)
                throw new IllegalArgumentException("Track number must be positive or negative, not zero");
            this.feature = feature;
            this.track = track;
        }
        public CompoundLocatedFeature getFeature() {
            return feature;
        }
        /**
         * Get the track this feature is located on.
         * In the case of a compound feature having more
         * than one component, subsequent components should
         * be placed on successive tracks starting with this
         * one
         * @return the track number: positive or negative, non-zero.
         */
        public int getTrack() {
            return track;
        }

        /**
         * Get the subfeature that sits on the specified track, if there is one.
         *
         * @param subfeatureTrack the track number
         * @return the subfeature on the specified track, or null if none
         */
        public LocatedFeature forTrack(int subfeatureTrack) {
            if (subfeatureTrack == 0)
                throw new IllegalArgumentException("Track number must be positive or negative, not zero");
            if ((track < 0 && subfeatureTrack > 0) || (track > 0 && subfeatureTrack < 0))
                return null;
            int subfeatureIndex;
            if (track > 0)
                subfeatureIndex = subfeatureTrack - track;
            else
                subfeatureIndex = track - subfeatureTrack;

            if (subfeatureIndex < 0 || subfeatureIndex >= feature.getSubfeatures().size())
                return null;

            return feature.getSubfeatures().get(subfeatureIndex);
        }

        public int compareTo (AllocatedCompoundFeature other) {
            return this.feature.compareTo(other.feature);
        }
    }

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
    protected void addFeature(CompoundLocatedFeature feature, int track) {
        allocatedCompoundFeatures.add(new AllocatedCompoundFeature(feature, track));

        int numberOfSubfeatures = feature.getSubfeatures().size();
        if (track + numberOfSubfeatures - 1 > numberOfPositiveTracks)
            numberOfPositiveTracks = track + numberOfSubfeatures - 1;
        else if (-track + numberOfSubfeatures - 1 > numberOfNegativeTracks)
            numberOfNegativeTracks = -track + numberOfSubfeatures - 1;
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
            LocatedFeature feature = f.forTrack(track);
            if (feature != null)
                ret.add(feature);
        }

        return ret;
    }

    @Deprecated
    public List<List<LocatedFeature>> getPositiveTracks() {
        List<List<LocatedFeature>> ret = new ArrayList<List<LocatedFeature>>();
        for (int i = 1; i <= numberOfPositiveTracks(); i++)
            ret.add(getTrack(i));
        return ret;
    }

    @Deprecated
    public List<List<LocatedFeature>> getNegativeTracks() {
        List<List<LocatedFeature>> ret = new ArrayList<List<LocatedFeature>>();
        for (int i = 1; i <= numberOfNegativeTracks(); i++)
            ret.add(getTrack(-i));
        return ret;
    }

    /**
     * Find the first gap large enough to contain <code>gapSize</code>
     * entries.
     *
     * @param filled the set of unavailable indices
     * @param gapSize the size of the required gap, >= 1
     * @return the index of the start of the first sufficiently-large gap
     */
    protected static int findGap(BitSet filled, int gapSize) {
        /*
         * Irrelevant note:
         *
         * This algorithm is linear-time and constant-space. I reckon it should
         * be possible to do better with a special-purpose data structure;
         * presumably there's a huge literature on this in the context of memory
         * management / allocation. We could probably improve performance simply by
         * using BitSet#nextClearBit(int). But we don't have a performance problem
         * here, as far as I know.
         */
        if (gapSize < 1)
            throw new IllegalArgumentException(String.format("gapSize is %d, must be >=1", gapSize));

        int currentGapSize = 0;
        for (int i = 0;; i++) {
            if (filled.get(i))
                currentGapSize = 0;
            else {
                if (++currentGapSize == gapSize)
                    return i - gapSize + 1;
            }
        }
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
     * @return the diagram Half
     */
    protected void allocateTracks(BoundarySet<? extends CompoundLocatedFeature> boundaries, boolean negativeHalf) {
        int numTracks = 0;
        BitSet activeTracks = new BitSet();
        Map<CompoundLocatedFeature, Integer> activeFeatures = new HashMap<CompoundLocatedFeature, Integer>();
        for (Boundary<? extends CompoundLocatedFeature> boundary : boundaries) {
            CompoundLocatedFeature feature = boundary.feature;
            int numSubfeatures = feature.getSubfeatures().size();
            if (numSubfeatures == 0) {
                /*
                 * Give a more comprehensible error message if this is a gene.
                 */
                if (feature instanceof BasicGene)
                    logger.error(String.format("The gene '%s' has no transcripts!", feature
                        .getUniqueName()));
                else
                    logger.error(String.format("The compound feature '%s' has no subfeatures!", feature
                            .getUniqueName()));
                continue;
            }

            int track, lastUsedTrack;
            switch (boundary.type) {
            case START:
                track = findGap(activeTracks, numSubfeatures);
                lastUsedTrack = track + numSubfeatures - 1;
                if (lastUsedTrack >= numTracks)
                    numTracks = lastUsedTrack + 1;
                for (int i = track; i <= lastUsedTrack; i++)
                    activeTracks.set(i);
                activeFeatures.put(feature, track);
                break;
            case END:
                track = activeFeatures.get(feature);
                lastUsedTrack = track + numSubfeatures - 1;
                addFeature(feature, (negativeHalf ? -1-track : 1+track));
                activeFeatures.remove(feature);

                for (int i = track; i <= lastUsedTrack; i++)
                    activeTracks.clear(i);
                break;
            default:
                throw new IllegalStateException(
                        "Boundary type is invalid. This should never happen!");
            }
        }
    }

}