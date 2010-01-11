package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A supercontig is a sequence assembled from multiple contigs,
 * which may or may not be explicitly stored in the database.
 * In cases where contigs <em>are</em> represented, suitable Contig and Gap
 * features are located on the supercontig. Features that belong to the
 * supercontig - genes and the like - have a primary featureloc (locgroup=0)
 * on the supercontig and, where applicable, have a secondary featureloc
 * (locgroup=1) on the contig.
 *
 * @author rh11
 */
@Entity
@FeatureType(cv = "sequence", term = "supercontig")
public class Supercontig extends TopLevelFeature {
    Supercontig() {
        // empty
    }

    public Supercontig(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
}
