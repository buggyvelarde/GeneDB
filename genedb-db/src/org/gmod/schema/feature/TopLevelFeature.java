package org.gmod.schema.feature;

import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A class representing a feature that may be a top level feature. (It could be an interface but
 * fits nicely into the hierarchy here so isn't)
 *
 *  It provides one method which indicates whether the given feature is actually a top-level feature.
 *  This helps distinguish cases eg where the project is (a) in contigs, or (b) in chromosomes but with
 *  contig features attached for tracking purposes.
 *
 * @author rh11
 */
@Entity
public abstract class TopLevelFeature extends Region {

    TopLevelFeature() {
        // empty
    }

    public TopLevelFeature(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    /**
     * Is this feature acting as a top-level feature in this case? Checks presence of
     * top_level_feature FeatureProp
     *
     * @return <code>true</code> if acting as a top-level feature ie a primary location reference
     */
    public boolean isTopLevelFeature() {
        return hasProperty("genedb_misc", "top_level_seq");
    }

}
