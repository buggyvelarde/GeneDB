package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="tRNA")
@Indexed
public class TRNA extends Transcript {

    @Override @Transient
    public Integer getColourId() {
        return null;
    }

}
