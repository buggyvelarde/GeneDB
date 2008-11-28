package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * A sequenced end of a BAC (bacterial artificial chromosome) clone, used
 * as a genetic marker. Typically BACEnds wll be located onto supercontigs or
 * chromosomes.
 *
 * @author rh11
 */
@Entity
@FeatureType(cv="sequence", accession="0000999")
public class BACEnd extends Read {

    public BACEnd() {
        super();
    }

    public BACEnd(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

}
