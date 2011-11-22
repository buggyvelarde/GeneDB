package org.gmod.schema.feature;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

@Entity
@FeatureType(cv="sequence", term="amino_acid")
public class AminoAcid extends Region {
	
    AminoAcid () {
        // empty
    }
    
    public AminoAcid(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public AminoAcid(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }
    
}
