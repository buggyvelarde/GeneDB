package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="gap")
@Indexed
public class Gap extends Region {
    Gap() {
        // empty
    }

    Gap(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    Gap(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    Gap(Organism organism, String uniqueName) {
        super(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }
}
