package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.sequence.Feature;

@FeatureType(cv="sequence", term="region")
@Entity
public class Region extends Feature {

}
