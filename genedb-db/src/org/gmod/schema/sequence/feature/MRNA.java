package org.gmod.schema.sequence.feature;


import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.cfg.FeatureType;
import org.hibernate.search.annotations.Indexed;

@Entity
@FeatureType(cv="sequence", term="mRNA")
@Indexed
public class MRNA extends ProductiveTranscript {
    private static final Logger logger = Logger.getLogger(MRNA.class);

    @Override @Transient
    public Polypeptide getProtein() {
        Polypeptide protein = super.getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide", getUniqueName(), getFeatureId()));
        }
        return protein;
    }

    @Override @Transient
    public Integer getColourId() {
        Polypeptide protein = getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide", getUniqueName(), getFeatureId()));
            return null;
        }
        return protein.getColourId();
    }
}
