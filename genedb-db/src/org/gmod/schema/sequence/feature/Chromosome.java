package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

/*
 * This should really be an abstract class. It is not though,
 * because there are actually 28 features in the database whose
 * type is plain 'chromosome'.
 */
@Entity
@FeatureType(cv="sequence", term="chromosome")
public class Chromosome extends TopLevelFeature {
    // Deliberately empty
}
