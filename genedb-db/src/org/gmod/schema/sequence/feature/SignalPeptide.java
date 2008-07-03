package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;

@Entity
@FeatureType(cv="sequence", term="signal_peptide")
public class SignalPeptide extends PolypeptideRegion {

    public SignalPeptide(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true /*analysis*/, false /*obsolete*/);
    }

}
