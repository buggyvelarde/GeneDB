package org.gmod.schema.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.hibernate.search.annotations.Indexed;

@Entity
@FeatureType(cv="sequence", term="modified_amino_acid_feature")
@Indexed
public class ModifiedAminoAcidFeature extends AminoAcid {
	
}
