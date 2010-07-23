package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A read is a continuous sequence obtained from a single experiment.
 * This class is abstract, because any read that is actually in the database
 * will either be a BACEnd or a ContigRead (and only the former is used
 * at present).
 *
 * @author rh11
 *
 */
@Entity
@FeatureType(cv="sequence", term="read")
public abstract class Read extends TopLevelFeature {

    public Read() {
        super();
    }

    public Read(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

}
