package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "intron")
public class Intron extends TranscriptRegion {

    Intron() {
        // empty
    }

    /*
     * This constructor is invoked reflectively by Transcript.createRegion.
     * All TranscriptRegions should have one.
     */
    public Intron(Organism organism, String uniqueName) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }

    public Intron(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public static Intron make(TopLevelFeature sourceFeature, String uniqueName, int fmin, int fmax) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Intron intron = new Intron(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(intron, fmin, fmax, (short) 0, 0);
        return intron;
    }

}
