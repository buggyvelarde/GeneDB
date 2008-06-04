package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.search.annotations.Indexed;

@Entity
@DiscriminatorValue("340")
@Indexed
public class TRNA extends Transcript {

    @Override
    public Integer getColourId() {
        return null;
    }

}
