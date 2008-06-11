package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/*
 * This should really be an abstract class. It is not though,
 * because there are actually 28 features in the database whose
 * type is plain 'chromosome'.
 */
@Entity
@DiscriminatorValue("427")
public class Chromosome extends TopLevelFeature {}