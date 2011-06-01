package org.gmod.schema.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="sequence", term="amino_acid")
public class AminoAcid extends Region {
	
}
