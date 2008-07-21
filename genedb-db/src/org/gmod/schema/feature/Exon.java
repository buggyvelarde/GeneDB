package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="exon")
public class Exon extends AbstractExon {

	public Exon(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}

	public static Exon make(Feature parent, StrandedLocation location,
			String systematicId, Organism organism, Timestamp now) {
		
		Exon exon = new Exon(organism, systematicId, false, false, now);
		parent.addLocatedChild(exon, location);
		return exon;
	}
	
}
