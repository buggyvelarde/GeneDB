package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="sequence", term="pseudogenic_exon")
public class PseudogenicExon extends AbstractExon {}
