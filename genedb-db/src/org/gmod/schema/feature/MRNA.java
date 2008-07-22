package org.gmod.schema.feature;


import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="mRNA")
@Indexed
public class MRNA extends ProductiveTranscript {
    private static final Logger logger = Logger.getLogger(MRNA.class);
    
    MRNA() {
        // empty
    }

	public MRNA(Organism organism, String systematicId, boolean analysis,
			boolean obsolete, Timestamp dateAccessioned) {
		super(organism, systematicId, analysis, obsolete, dateAccessioned);
	}

	public static MRNA make(Feature parent, StrandedLocation location,
			String systematicId, Organism organism, Timestamp now) {
		
		MRNA mRNA = new MRNA(organism, systematicId, false, false, now);
		//mRNA.persist();
		parent.addLocatedChild(mRNA, location);
		
		return mRNA;
	}
	
//	static MRNA make(Feature parent, Location loc, Organism organism, String systematicId, String type, Timestamp now) {
//		
//		MRNA mrna = new MRNA(organism, systematicId, false, false, now);
//		parent.addLocatedChild(mRNA, location);
//		return mrna;
//	}
    
    @Override @Transient
    public Polypeptide getProtein() {
        Polypeptide protein = super.getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide", getUniqueName(), getFeatureId()));
        }
        return protein;
    }

    @Override @Transient
    public Integer getColourId() {
        Polypeptide protein = getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide", getUniqueName(), getFeatureId()));
            return null;
        }
        return protein.getColourId();
    }
}
