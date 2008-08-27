package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="linear_double_stranded_DNA_chromosome")
public class LinearDoubleStrandedDNAChromosome extends Chromosome {

    LinearDoubleStrandedDNAChromosome() {
        // empty
    }

    public LinearDoubleStrandedDNAChromosome(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

}
