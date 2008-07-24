package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="three_prime_UTR")
public class ThreePrimeUTR extends UTR {

    ThreePrimeUTR() {
    // empty
    }

    public ThreePrimeUTR(Organism organism, String systematicId, boolean analysis,
        boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

    public static ThreePrimeUTR make(Feature parent, StrandedLocation location,
        String systematicId, Organism organism, Timestamp now) {

        ThreePrimeUTR utr = new ThreePrimeUTR(organism, systematicId, false, false, now);
        parent.addLocatedChild(utr, location);
        return utr;
    }

}
