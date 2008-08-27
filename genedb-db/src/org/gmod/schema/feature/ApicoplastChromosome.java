package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/*
 * Note: we are using the 'apicoplast_sequence' SO term, which
 * is NOT correct for this purpose. It's not even the correct type. This
 * needs to be changed in the database, the loader and here. The problem
 * is that there is no obviously-suitable term, though we could simply use
 * circular_double_stranded_DNA_chromosome to record the DNA type but not
 * the origin.
 */
@Entity
@FeatureType(cv = "sequence", term = "apicoplast_sequence")
public class ApicoplastChromosome extends Chromosome {

    ApicoplastChromosome() {
        // empty
    }

    public ApicoplastChromosome(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }
}
