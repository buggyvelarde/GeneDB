package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="pseudogenic_exon")
public class PseudogenicExon extends AbstractExon {}
