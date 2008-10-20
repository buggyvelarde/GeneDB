package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A part of a match, for example an hsp from blast is a match_part.
 * <code>SO:0000039</code>
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", term="match_part")
public class MatchPart extends Region {

    public MatchPart() {
        super();
    }

    public MatchPart(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public MatchPart(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public MatchPart(Organism organism, String uniqueName) {
        this(organism, uniqueName, true, false, new Timestamp(System.currentTimeMillis()));
    }

}
