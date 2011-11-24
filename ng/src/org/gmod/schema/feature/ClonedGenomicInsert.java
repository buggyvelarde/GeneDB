package org.gmod.schema.feature;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

@Entity
@FeatureType(cv="sequence", term="cloned_genomic_insert")
public class ClonedGenomicInsert extends CloneInsert {

    public ClonedGenomicInsert(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis, boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }
    
    public ClonedGenomicInsert(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

}
