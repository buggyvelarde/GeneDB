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

    public Contig(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public static Contig make(String uniqueName, Organism organism) {
        return new Contig(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }
}
