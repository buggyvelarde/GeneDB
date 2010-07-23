package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * Used to represent microsatellite markers in Schistosoma mansoni.
 * (Could be used for any PCR product for which no more appropriate term
 * exists, of course.)
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", term="PCR_product")
public class PCRProduct extends Reagent {

    PCRProduct() {
        super();
    }

    public PCRProduct(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public PCRProduct(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

}
