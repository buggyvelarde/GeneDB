package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/*
 * The term name has recently changed from 'non_cytoplasm_location' to 'non_cytoplasmic_region'
 */
@Entity
@FeatureType(cv="sequence", accession="0001074")
public class NonCytoplasmicRegion extends ExtramembraneRegion {

    NonCytoplasmicRegion() {
        // empty
    }

    public NonCytoplasmicRegion(Organism organism, CvTerm cvTerm, String uniqueName) {
        this(organism, cvTerm, uniqueName, true, false);
    }

    public NonCytoplasmicRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public NonCytoplasmicRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete) {
        super(organism, cvTerm, uniqueName, analysis, obsolete);
    }

}
