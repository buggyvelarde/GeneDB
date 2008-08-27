package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="three_prime_UTR")
public class ThreePrimeUTR extends UTR {

    ThreePrimeUTR() {
    // empty
    }

    public ThreePrimeUTR(Organism organism, String uniqueName, boolean analysis,
        boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
}
