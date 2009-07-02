package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="genomic_island")
public class GenomicIsland extends Region {
    // Deliberately empty
}
