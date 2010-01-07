package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="sequence_alteration")
public abstract class SequenceAlteration extends Feature {

    public SequenceAlteration() {
        super();
    }

    public SequenceAlteration(Organism organism, CvTerm cvTerm, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp timeAccessioned,
            Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public SequenceAlteration(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned) {
        super(organism, uniqueName, analysis, obsolete, timeAccessioned, timeAccessioned);
    }

}
