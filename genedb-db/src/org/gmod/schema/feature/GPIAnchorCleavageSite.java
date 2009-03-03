package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;
import javax.persistence.Transient;

/*
 * There isn't yet a suitable SO term for this, so we're temporarily
 * using a local term. When this term is added to SO, we should switch
 * to using the SO term instead.
 */
@Entity
@FeatureType(cv="genedb_feature_type", term="GPI_anchor_cleavage_site")
public class GPIAnchorCleavageSite extends PolypeptideRegion {

    GPIAnchorCleavageSite() {
        // Deliberately empty
    }
    
    
    public GPIAnchorCleavageSite(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true /*analysis*/, false /*obsolete*/);
    }

    @Transient @Override
    public String getScore() {
        return getProperty("genedb_misc", "GPI_cleavage_site_score");
    }
}
