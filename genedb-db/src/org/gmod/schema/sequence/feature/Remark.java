package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.sequence.FeatureProp;

import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * A remark about a portion of the sequence.
 * Should have a FeatureProperty of type 'comment'.
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", term="remark")
public class Remark extends Region {
    private static final Logger logger = Logger.getLogger(Remark.class);

    /**
     * Get the comment attached to this Remark feature.
     * @return the comment, or null if no comment was found
     */
    @Transient
    public String getComment() {
        for(FeatureProp featureProp: this.getFeatureProps()) {
            if (featureProp.getCvTerm().getName().equals("comment")) {
                return featureProp.getValue();
            }
        }

        logger.error(String.format("Remark feature '%s' (ID=%d) has no comment",
            this.getUniqueName(), this.getFeatureId()));
        return null;
    }
}
