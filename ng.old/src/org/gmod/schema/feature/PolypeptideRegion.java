package org.gmod.schema.feature;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Transient;

/**
 * A region of a polypeptide.
 * <p>
 * All concrete implementing classes must have a three-argument constructor
 * with arguments <code>(Organism organism, CvTerm type, String uniqueName)</code>.
 * This constructor is invoked introspectively by {@link SequenceDao#createPolypeptideRegion()}.
 *
 * @author rh11
 */
@FeatureType(cv="sequence", accession="0000839")
public abstract class PolypeptideRegion extends Region {

    PolypeptideRegion() {
        // empty
    }

    PolypeptideRegion(Organism organism, String uniqueName) {
        this(organism, uniqueName, /*analysis:*/true, /*obsolete:*/false,
            new Timestamp(System.currentTimeMillis()));
    }

    public PolypeptideRegion(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public PolypeptideRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public PolypeptideRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete) {
        // Constructor call must be the first statement in a constructor,
        // hence the duplicated Timestamp construction.
        super(organism, cvTerm, uniqueName, analysis, obsolete,
            new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }

    @Transient
    public String getScore() {
        return this.getProperty("null", "score");
    }
}
