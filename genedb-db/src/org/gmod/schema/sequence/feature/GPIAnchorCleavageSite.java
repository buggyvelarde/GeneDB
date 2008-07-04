package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="genedb_feature_type", term="GPI_anchor_cleavage_site")
public class GPIAnchorCleavageSite extends PolypeptideRegion { }
