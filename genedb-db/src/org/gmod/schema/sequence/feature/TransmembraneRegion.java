package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;

@Entity
@FeatureType(cv="sequence", accession="0001077")
public class TransmembraneRegion extends PolypeptideRegion {

    public TransmembraneRegion() {
        super();
    }

    public TransmembraneRegion(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true, false);
    }

}
