package org.gmod.schema.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="sequence", term="modified_amino_acid_feature")
public class ModifiedAminoAcidFeature extends AminoAcid {
	
}
