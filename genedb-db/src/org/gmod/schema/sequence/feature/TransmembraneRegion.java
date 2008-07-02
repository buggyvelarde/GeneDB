package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;

@Entity
@DiscriminatorValue("1164")
public class TransmembraneRegion extends PolypeptideRegion {

    public TransmembraneRegion() {
        super();
    }

    public TransmembraneRegion(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true, false);
    }

}
