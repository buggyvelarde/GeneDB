package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.gmod.schema.sequence.Feature;

@Entity
@DiscriminatorValue("275")
public class Intron extends Feature { }
