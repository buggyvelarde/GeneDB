package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "contig")
public class Contig extends TopLevelFeature {
    Contig() {
        // empty
    }

    public Contig(Organism organism, String systematicId, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

}
