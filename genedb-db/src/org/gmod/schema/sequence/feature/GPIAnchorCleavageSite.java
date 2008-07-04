package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

/*
 * There isn't yet a suitable SO term for this, so we're temporarily
 * using a local term. When this term is added to SO, we should switch
 * to using the SO term instead.
 */
@Entity
@FeatureType(cv="genedb_feature_type", term="GPI_anchor_cleavage_site")
public class GPIAnchorCleavageSite extends PolypeptideRegion { }
