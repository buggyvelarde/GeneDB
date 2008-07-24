package org.gmod.schema.feature;

import java.sql.Timestamp;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="long terminal repeat")
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
