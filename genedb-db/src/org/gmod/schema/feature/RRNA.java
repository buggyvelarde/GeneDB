package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv = "sequence", term = "rRNA")
@Indexed
public class RRNA extends Transcript {

    RRNA() {
        // empty
    }

    public RRNA(Organism organism, String systematicId, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

    @Override
    @Transient
    public Integer getColourId() {
        return null;
    }

}
