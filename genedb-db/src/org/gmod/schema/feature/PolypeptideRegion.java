package org.gmod.schema.feature;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

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
