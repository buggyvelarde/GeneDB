package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.biojava.bio.symbol.Location;
import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="intron")
public class Intron extends TranscriptRegion {
	
	public Intron(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}
	

	public static Intron make(Feature parent, Location exonLocation,
			String systematicId, Organism organism, Timestamp now) {
		
		Intron intron = new Intron(organism, systematicId, false, false, now);      
		return intron;
	}
	
}
