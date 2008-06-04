package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("339")
@Indexed
public class RRNA extends Transcript {

    @Override
    public Integer getColourId() {
        return null;
    }

}
