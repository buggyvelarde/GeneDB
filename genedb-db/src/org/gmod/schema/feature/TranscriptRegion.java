package org.gmod.schema.feature;


import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * SO:0000833 (but we don't use it directly, which is why it's an
 * abstract class).
 *
 * @author rh11
 */
@Entity
public abstract class TranscriptRegion extends Region {

    TranscriptRegion() {
        // empty
    }

    public TranscriptRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public TranscriptRegion(Organism organism, String systematicId, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

}
