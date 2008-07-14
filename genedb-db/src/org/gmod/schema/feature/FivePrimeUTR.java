package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="five_prime_UTR")
public class FivePrimeUTR extends UTR {}
