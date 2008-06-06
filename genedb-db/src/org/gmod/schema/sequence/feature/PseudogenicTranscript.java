package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("604")
@Indexed
public class PseudogenicTranscript extends Transcript {

    @Override @Transient
    public Integer getColourId() {
        return null; // TODO
    }

}
