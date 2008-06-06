package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/*
 * Note: this value 831 refers to the 'apicoplast_sequence' SO term, which
 * is NOT correct for this purpose. It's not even the correct type. This
 * needs to be changed in the database, the loader and here. The problem
 * is that there is no obviously-suitable term, though we could simply use
 * circular_double_stranded_DNA_chromosome to record the DNA type but not
 * the origin.
 */
@Entity
@DiscriminatorValue("831")
public class ApicoplastChromosome extends Chromosome {}
