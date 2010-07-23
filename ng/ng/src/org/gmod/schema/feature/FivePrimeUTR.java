package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

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

    /*
     * This constructor is invoked reflectively by Transcript.createRegion.
     * All TranscriptRegions should have one.
     */
    public FivePrimeUTR(Organism organism, String uniqueName) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }

    public static FivePrimeUTR make(TopLevelFeature sourceFeature, String uniqueName, int fmin, int fmax) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        FivePrimeUTR fpu = new FivePrimeUTR(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(fpu, fmin, fmax, (short) 0, 0);
        return fpu;
    }
}
