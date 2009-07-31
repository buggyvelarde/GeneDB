package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", accession="0000182")
public class DNATransposon extends TransposableElement {
    DNATransposon() {
        // empty
    }

    public DNATransposon(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public DNATransposon(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }
}