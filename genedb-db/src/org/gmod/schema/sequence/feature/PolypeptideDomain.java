package org.gmod.schema.sequence.feature;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureProp;

@Entity
@DiscriminatorValue("504")
public class PolypeptideDomain extends Feature {
    private static final Logger logger = Logger.getLogger(PolypeptideDomain.class);

    public PolypeptideDomain() { }
    public PolypeptideDomain(Organism organism, CvTerm type, String uniqueName) {
        this(organism, type, uniqueName, false, false);
    }
    public PolypeptideDomain(Organism organism, CvTerm type, String uniqueName, boolean analysis,
            boolean obsolete) {
        // Constructor call must be the first statement in a constructor,
        // hence the duplicated Timestamp construction.
        super(organism, type, uniqueName, analysis, obsolete,
            new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
    }

    @Transient
    public String getScore() {
        for (FeatureProp featureProp : this.getFeatureProps())
            if (featureProp.getCvTerm().getName().equals("score"))
                return featureProp.getValue();

        logger.error(String.format("Polypeptide domain '%s' has no score", getUniqueName()));
        return null;
    }

    @Transient
    public DbXRef getInterProDbXRef() {
        for (FeatureDbXRef featureDbXRef: this.getFeatureDbXRefs()) {
            DbXRef dbXRef = featureDbXRef.getDbXRef();
            if ("InterPro".equals(dbXRef.getDb().getName()))
                return dbXRef;
        }
        return null;
    }
}
