package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "nucleotide_match")
public class NucleotideMatch extends Match {

    NucleotideMatch() {
        super();
    }

    public NucleotideMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public NucleotideMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

}
