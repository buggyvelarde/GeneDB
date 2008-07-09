package org.gmod.schema.sequence.feature;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;

import java.sql.Timestamp;
import java.util.Date;

public class PolypeptideRegion extends Region {

    public PolypeptideRegion() {}

    public PolypeptideRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public PolypeptideRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete) {
        // Constructor call must be the first statement in a constructor,
        // hence the duplicated Timestamp construction.
        super(organism, cvTerm, uniqueName, analysis, obsolete,
            new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
    }
}
