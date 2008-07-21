package org.gmod.schema.feature;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.gmod.schema.mapped.Organism;

/**
 * An {@link Exon} or {@link PseudogenicExon}.
 *
 * @author rh11
 *
 */
@Entity
public abstract class AbstractExon extends TranscriptRegion {
	
	public AbstractExon() {
		// Deliberately empty
	}
	
	public AbstractExon(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}
}
