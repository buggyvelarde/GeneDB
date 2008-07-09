package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="repeat_region")
public class RepeatRegion extends Region { }
