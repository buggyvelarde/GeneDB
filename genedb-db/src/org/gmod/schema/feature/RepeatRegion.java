package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="repeat_region")
public class RepeatRegion extends Region {
    RepeatRegion() {
        // empty
    }
}
