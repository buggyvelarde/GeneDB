package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="centromere")
public class Centromere extends Region {

    Centromere() {
        // empty
    }

    public Centromere(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public Centromere make(TopLevelFeature sourceFeature, int fmin, int fmax) {
        String centromereUniqueName = String.format("%s:centromere", sourceFeature.getUniqueName());
        return make(sourceFeature, centromereUniqueName, fmin, fmax);
    }
    public static Centromere make(TopLevelFeature sourceFeature, String uniqueName, int fmin, int fmax) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Centromere centromere = new Centromere(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(centromere, fmin, fmax, (short) 0, 0);
        return centromere;
    }
}
