package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="polypeptide_domain")
public class PolypeptideDomain extends PolypeptideRegion {
    private static final Logger logger = Logger.getLogger(PolypeptideDomain.class);

    public PolypeptideDomain() { }
    public PolypeptideDomain(Organism organism, CvTerm type, String uniqueName) {
        this(organism, type, uniqueName, true, false);
    }
    public PolypeptideDomain(Organism organism, CvTerm type, String uniqueName, boolean analysis,
            boolean obsolete) {
        super(organism, type, uniqueName, analysis, obsolete);
    }

    @Transient
    public String getScore() {
        for (FeatureProp featureProp : this.getFeatureProps()) {
            if (featureProp.getType().getName().equals("score")) {
                return featureProp.getValue();
            }
        }

        logger.error(String.format("Polypeptide domain '%s' has no score", getUniqueName()));
        return null;
    }

    @Transient
    public DbXRef getInterProDbXRef() {
        for (FeatureDbXRef featureDbXRef: this.getFeatureDbXRefs()) {
            DbXRef dbXRef = featureDbXRef.getDbXRef();
            if ("InterPro".equals(dbXRef.getDb().getName())) {
                return dbXRef;
            }
        }
        return null;
    }
}
