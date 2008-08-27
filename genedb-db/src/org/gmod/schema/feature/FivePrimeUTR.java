package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="five_prime_UTR")
public class FivePrimeUTR extends UTR {

    FivePrimeUTR() {
        // empty
    }

    public FivePrimeUTR(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public static FivePrimeUTR make(Feature parent, StrandedLocation location,
            String uniqueName, Organism organism, Timestamp now) {

        FivePrimeUTR utr = new FivePrimeUTR(organism, uniqueName, false, false, now);
        parent.addLocatedChild(utr, location);
        return utr;
    }

}
