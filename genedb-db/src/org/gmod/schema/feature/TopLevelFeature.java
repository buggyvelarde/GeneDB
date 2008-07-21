package org.gmod.schema.feature;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.gmod.schema.mapped.Organism;

/**
 * A chromosome or contig.
 *
 * @author rh11
 */
@Entity
public abstract class TopLevelFeature extends Region { 
	
	// TODO Should this be an interface
	
	public TopLevelFeature(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}
	
	
}
