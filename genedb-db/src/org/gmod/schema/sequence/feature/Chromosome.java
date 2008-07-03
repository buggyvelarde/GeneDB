package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

/*
 * This should really be an abstract class. It is not though,
 * because there are actually 28 features in the database whose
 * type is plain 'chromosome'.
 */
@Entity
@FeatureType(cv="sequence", term="chromosome")
public class Chromosome extends TopLevelFeature {}