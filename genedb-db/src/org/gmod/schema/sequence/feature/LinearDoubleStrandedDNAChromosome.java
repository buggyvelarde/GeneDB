package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;

@Entity
@FeatureType(cv="sequence", term="linear_double_stranded_DNA_chromosome")
public class LinearDoubleStrandedDNAChromosome extends Chromosome { }
