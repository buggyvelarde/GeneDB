package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.gmod.schema.sequence.Feature;

@Entity
@DiscriminatorValue("665")
public class Centromere extends Feature {}
