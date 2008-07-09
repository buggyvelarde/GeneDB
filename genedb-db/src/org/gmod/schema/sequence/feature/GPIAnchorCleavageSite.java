package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;

/*
 * There isn't yet a suitable SO term for this, so we're temporarily
 * using a local term. When this term is added to SO, we should switch
 * to using the SO term instead.
 */
@Entity
@FeatureType(cv="genedb_feature_type", term="GPI_anchor_cleavage_site")
public class GPIAnchorCleavageSite extends PolypeptideRegion {
    public GPIAnchorCleavageSite() {}
    public GPIAnchorCleavageSite(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true /*analysis*/, false /*obsolete*/);
    }

}
