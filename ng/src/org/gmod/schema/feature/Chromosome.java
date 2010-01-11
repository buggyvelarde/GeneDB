package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/*
 * This should really be an abstract class. It is not though,
 * because there are actually 28 features in the database whose
 * type is plain 'chromosome'.
 */
@Entity
@FeatureType(cv="sequence", term="chromosome")
public class Chromosome extends TopLevelFeature {

    Chromosome() {
        // empty
    }

    public Chromosome(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
}
