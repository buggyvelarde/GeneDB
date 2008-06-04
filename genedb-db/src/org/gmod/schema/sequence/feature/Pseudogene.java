package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("423")
@Indexed
public class Pseudogene extends AbstractGene {

}
