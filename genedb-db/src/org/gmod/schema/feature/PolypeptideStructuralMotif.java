package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

@FeatureType(cv="sequence", accession="0001079")
public abstract class PolypeptideStructuralMotif extends PolypeptideRegion {

    PolypeptideStructuralMotif() {
    }

    public PolypeptideStructuralMotif(Organism organism, String uniqueName) {
        super(organism, uniqueName);
    }

    public PolypeptideStructuralMotif(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public PolypeptideStructuralMotif(Organism organism, CvTerm cvTerm, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp timeAccessioned,
            Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public PolypeptideStructuralMotif(Organism organism, CvTerm cvTerm, String uniqueName,
            boolean analysis, boolean obsolete) {
        super(organism, cvTerm, uniqueName, analysis, obsolete);
    }

}
