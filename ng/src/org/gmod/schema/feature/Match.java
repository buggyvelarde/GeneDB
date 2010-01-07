package org.gmod.schema.feature;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Transient;

/**
 * A match feature should have two featureLocs:
 * one, with rank 0, on the query;
 * and one, with rank 1, on the target.
 *
 * @author rh11
 */
public abstract class Match extends Region {
    Match() {
        // empty
    }

    public Match(Organism organism, String uniqueName) {
        this(organism, uniqueName, true, false);
    }

    public Match(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete, new Timestamp(System.currentTimeMillis()));
    }

    @Transient
    public Iterable<MatchPart> getParts() {
        SortedSet<MatchPart> parts = new TreeSet<MatchPart>();

        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getSubjectFeature();
            if (feature instanceof MatchPart) {
                parts.add((MatchPart) feature);
            }
        }

        return parts;
    }

    public MatchPart createPart(String uniqueName, int sourcePos, int sourceLength, int targetPos, int targetLength) {
        FeatureLoc sourceLoc = this.getFeatureLoc(0, 0);
        FeatureLoc targetLoc = this.getFeatureLoc(0, 1);

        if (sourceLoc == null) {
            throw new IllegalStateException(String.format("Match feature '%s' has no source (rank=0) featureLoc",
                getUniqueName()));
        }
        if (sourcePos < 0) {
            throw new IllegalArgumentException(String.format("Supplied sourcePos (%d) is less than zero",
                sourcePos));
        }
        if (sourcePos + sourceLength > sourceLoc.getLength()) {
            throw new IllegalArgumentException(String.format(
                "Supplied sourcePos (%d) + sourceLength (%d) is greater than source length (%d)",
                sourcePos, sourceLength, sourceLoc.getLength()));
        }

        if (targetLoc == null) {
            throw new IllegalStateException(String.format("Match feature '%s' has no target (rank=1) featureLoc",
                getUniqueName()));
        }
        if (targetPos < 0) {
            throw new IllegalArgumentException(String.format("Supplied targetPos (%d) is less than zero",
                targetPos));
        }
        if (targetPos + targetLength > targetLoc.getLength()) {
            throw new IllegalArgumentException(String.format(
                "Supplied targetPos (%d) + targetLength (%d) is greater than target length (%d)",
                targetPos, targetLength, targetLoc.getLength()));
        }

        MatchPart matchPart = new MatchPart(this.getOrganism(), uniqueName);
        this.addFeatureRelationship(matchPart, "relationship", "part_of");

        sourceLoc.getSourceFeature().addLocatedChild(matchPart, sourceLoc.getFmin() + sourcePos,
            sourceLoc.getFmin() + sourcePos + sourceLength,
            sourceLoc.getStrand(), null, 0, 0);
        targetLoc.getSourceFeature().addLocatedChild(matchPart, targetLoc.getFmin() + targetPos,
            targetLoc.getFmin() + targetPos + targetLength,
            targetLoc.getStrand(), null, 0, 1);

        return matchPart;
    }

    /**
     * Delete this match and all its parts.
     */
    @Override
    public void delete() {
        for (MatchPart part: getParts()) {
            part.delete();
        }
        super.delete();
    }
}
