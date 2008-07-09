package org.gmod.schema.sequence.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="linear_double_stranded_DNA_chromosome")
public class LinearDoubleStrandedDNAChromosome extends Chromosome { }
