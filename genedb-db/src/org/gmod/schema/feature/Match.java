package org.gmod.schema.feature;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Transient;


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
