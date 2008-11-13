package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="protein_match")
public class ProteinMatch extends Match {
    ProteinMatch() {
        // empty
    }

    public ProteinMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public ProteinMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }
}
