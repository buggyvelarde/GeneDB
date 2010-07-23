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

    /*
     * This constructor is invoked reflectively by Transcript.createRegion.
     * All TranscriptRegions should have one.
     */
    public ThreePrimeUTR(Organism organism, String uniqueName) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }

    public static ThreePrimeUTR make(TopLevelFeature sourceFeature, String uniqueName, int fmin, int fmax) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ThreePrimeUTR tpu = new ThreePrimeUTR(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(tpu, fmin, fmax, (short) 0, 0);
        return tpu;
    }
}
