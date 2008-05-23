package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;

import org.hibernate.search.annotations.Indexed;

@DiscriminatorValue("423")
@Indexed
public class Pseudogene extends AbstractGene {

}
