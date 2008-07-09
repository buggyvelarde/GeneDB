package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.sequence.Feature;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="polypeptide_motif")
public class PolypeptideMotif extends PolypeptideDomain { }
