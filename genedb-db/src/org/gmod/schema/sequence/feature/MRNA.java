package org.gmod.schema.sequence.feature;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("321")
@Indexed
public class MRNA extends ProductiveTranscript { }
