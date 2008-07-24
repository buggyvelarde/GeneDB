package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "long_terminal_repeat")
@Indexed
public class LongTerminalRepeat extends RepeatRegion {

    LongTerminalRepeat() {
        // empty
    }

    public LongTerminalRepeat(Organism organism, String systematicId, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

}
