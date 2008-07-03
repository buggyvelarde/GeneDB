package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="sequence", term="three_prime_UTR")
public class ThreePrimeUTR extends UTR {}
