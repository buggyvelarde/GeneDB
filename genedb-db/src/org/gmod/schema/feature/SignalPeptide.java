package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="signal_peptide")
public class SignalPeptide extends PolypeptideRegion {
    private static final Logger logger = Logger.getLogger(SignalPeptide.class);

    // Constructors

    SignalPeptide() {
        // Deliberately empty default constructor
    }

    public SignalPeptide(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true /*analysis*/, false /*obsolete*/);
    }

    /**
     * Get the probability of this cleavage site, as predicted by some
     * algorithm such as DGPI.
     *
     * @return a string containing the probability of this cleavage site,
     *         or null if there is none.
     */
    @Transient
    public String getProbability() {
        String cleavageSiteProbability = this.getProperty("genedb_misc", "cleavage_site_probability");
        if (cleavageSiteProbability == null) {
            logger.error(String.format("Signal peptide '%s' has no cleavage_site_probability",
                this.getUniqueName()));
        }
        return cleavageSiteProbability;
    }

}
