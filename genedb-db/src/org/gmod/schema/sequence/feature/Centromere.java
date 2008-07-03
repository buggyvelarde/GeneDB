package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.sequence.Feature;

@Entity
@FeatureType(cv="sequence", term="centromere")
public class Centromere extends Feature {}
