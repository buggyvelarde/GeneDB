package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "EST_match")
public class ESTMatch extends Match {

    ESTMatch() {
        super();
    }

    public ESTMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public ESTMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

}
