package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "repeat_unit")
public class RepeatUnit extends RepeatRegion {

    RepeatUnit() {
        // empty
    }

    public RepeatUnit(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public RepeatUnit(Organism organism, String uniqueName, String name) {
        super(organism, uniqueName, name);
    }

}
