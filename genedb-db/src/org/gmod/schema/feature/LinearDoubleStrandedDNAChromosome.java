package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="linear_double_stranded_DNA_chromosome")
public class LinearDoubleStrandedDNAChromosome extends Chromosome {

    LinearDoubleStrandedDNAChromosome() {
        // empty
    }

    public LinearDoubleStrandedDNAChromosome(Organism organism, String systematicId, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

}
