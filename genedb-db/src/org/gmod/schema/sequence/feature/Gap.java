package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.gmod.schema.sequence.Feature;
import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("818")
@Indexed
public class Gap extends Feature {}
