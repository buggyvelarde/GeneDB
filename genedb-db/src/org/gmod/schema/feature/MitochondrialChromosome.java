package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="mitochondrial_chromosome")
public class MitochondrialChromosome extends Chromosome {
	
	public MitochondrialChromosome(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}
	
}
