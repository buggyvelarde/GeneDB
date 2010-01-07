package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv = "sequence", term = "mRNA")
@Indexed
public class MRNA extends ProductiveTranscript {
    private static final Logger logger = Logger.getLogger(MRNA.class);

    MRNA() {
        // empty
    }

    public MRNA(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    MRNA(Organism organism, String uniqueName, String name) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        setName(name);
    }

    @Override
    @Transient
    public Polypeptide getProtein() {
        Polypeptide protein = super.getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide",
                getUniqueName(), getFeatureId()), new Throwable("Stack trace"));
        }
        return protein;
    }

    @Override
    @Transient
    public Integer getColourId() {
        Polypeptide protein = getProtein();
        if (protein == null) {
            logger.error(String.format("The mRNA transcript '%s' (ID=%d) has no polypeptide",
                getUniqueName(), getFeatureId()));
            return null;
        }
        return protein.getColourId();
    }
}
