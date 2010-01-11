package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="tandem_repeat")
public class TandemRepeat extends RepeatRegion {

    TandemRepeat() {
        super();
    }

    public TandemRepeat(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public TandemRepeat(Organism organism, String uniqueName, String name) {
        super(organism, uniqueName, name);
    }

}
