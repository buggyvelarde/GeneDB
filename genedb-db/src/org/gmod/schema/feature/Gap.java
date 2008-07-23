package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="gap")
@Indexed
public class Gap extends Region {
    Gap() {
        // empty
    }
}
