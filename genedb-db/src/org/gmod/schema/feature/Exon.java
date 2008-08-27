package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="exon")
public class Exon extends AbstractExon {

    Exon() {
        // empty
    }

    public Exon(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public static Exon make(Feature sourceFeature, StrandedLocation location,
            String uniqueName, Timestamp now) {

        Exon exon = new Exon(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(exon, location);
        return exon;
    }

}
