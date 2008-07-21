package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.biojava.bio.symbol.Location;
import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

/*
 * This should really be an abstract class. It is not though,
 * because there are actually 28 features in the database whose
 * type is plain 'chromosome'.
 */
@Entity
@FeatureType(cv="sequence", term="chromosome")
public class Chromosome extends TopLevelFeature {
    // Deliberately empty
	

	public Chromosome(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}

	public static Chromosome make(String exonSystematicId, Organism organism, Timestamp now) {
		
		Chromosome chromosome = new Chromosome(organism, exonSystematicId, false, false, now);      
		return chromosome;
	}
	
}
