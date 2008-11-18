package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="protein_match")
public class ProteinMatch extends Match {
    private static final Logger logger = Logger.getLogger(ProteinMatch.class);

    ProteinMatch() {
        // empty
    }

    public ProteinMatch(Organism organism, String uniqueName, boolean analysis, boolean obsolete) {
        super(organism, uniqueName, analysis, obsolete);
    }

    public ProteinMatch(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

    public Region getQuery() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 0);
        if (featureLoc == null) {
            logger.error(String.format("Could not find query featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature queryFeature = featureLoc.getSourceFeature();
        if (queryFeature == null) {
            logger.error(String.format("Could not find query feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (queryFeature instanceof Region) ) {
            logger.error(String.format("Query feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Region",
                queryFeature.getUniqueName(), queryFeature.getFeatureId(), getUniqueName(), getFeatureId(), queryFeature.getClass()));
            return null;
        }
        return (Region) queryFeature;
    }

    public Polypeptide getTarget() {
        FeatureLoc featureLoc = this.getFeatureLoc(0, 1);
        if (featureLoc == null) {
            logger.error(String.format("Could not find target featureloc for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        Feature targetFeature = featureLoc.getSourceFeature();
        if (targetFeature == null) {
            logger.error(String.format("Could not find target feature for ProteinMatch '%s' (ID=%d)",
                getUniqueName(), getFeatureId()));
            return null;
        }
        if (! (targetFeature instanceof Polypeptide)) {
            logger.error(String.format("Target feature '%s' (ID=%d) for ProteinMatch '%s' (ID=%d) is %s, not Polypeptide",
                targetFeature.getUniqueName(), targetFeature.getFeatureId(), getUniqueName(), getFeatureId(), targetFeature.getClass()));
            return null;
        }
        return (Polypeptide) targetFeature;
    }
}
