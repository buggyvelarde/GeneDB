package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.hibernate.search.annotations.Indexed;

@Entity
@FeatureType(cv="sequence", term="gap")
@Indexed
public class Gap extends Region {}
