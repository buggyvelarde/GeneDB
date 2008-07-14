package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="snRNA")
@Indexed
public class SnRNA extends Transcript {

    @Override @Transient
    public Integer getColourId() {
        return null;
    }

}
