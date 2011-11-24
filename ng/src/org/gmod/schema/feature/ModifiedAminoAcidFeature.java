package org.gmod.schema.feature;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;
import org.hibernate.search.annotations.Indexed;

@Entity
@FeatureType(cv="sequence", term="modified_amino_acid_feature")
@Indexed
public class ModifiedAminoAcidFeature extends AminoAcid {
    
    ModifiedAminoAcidFeature() {
        // empty
    }
    
    public ModifiedAminoAcidFeature(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public ModifiedAminoAcidFeature(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }
}
