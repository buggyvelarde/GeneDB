package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="pseudogenic_transcript")
@Indexed
public class PseudogenicTranscript extends ProductiveTranscript {
	PseudogenicTranscript() {
	    // empty
	}

	public PseudogenicTranscript(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}

}
