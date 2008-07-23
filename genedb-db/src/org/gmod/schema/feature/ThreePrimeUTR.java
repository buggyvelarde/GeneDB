package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="three_prime_UTR")
public class ThreePrimeUTR extends UTR {
    ThreePrimeUTR() {
        // empty
    }
}
