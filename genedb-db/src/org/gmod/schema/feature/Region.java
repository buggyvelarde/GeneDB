package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@FeatureType(cv="sequence", term="region")
@Entity
public class Region extends Feature {
    public Region() {}
    public Region(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

}
