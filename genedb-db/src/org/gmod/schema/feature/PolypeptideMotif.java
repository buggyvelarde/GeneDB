package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="polypeptide_motif")
public class PolypeptideMotif extends PolypeptideDomain { }
