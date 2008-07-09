package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="repeat_region")
public class RepeatRegion extends Region { }
