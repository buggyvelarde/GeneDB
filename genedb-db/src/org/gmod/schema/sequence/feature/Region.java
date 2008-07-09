package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;

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
