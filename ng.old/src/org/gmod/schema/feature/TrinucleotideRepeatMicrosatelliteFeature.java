package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="trinucleotide_repeat_microsatellite_feature")
public class TrinucleotideRepeatMicrosatelliteFeature extends Microsatellite {

    TrinucleotideRepeatMicrosatelliteFeature() {
        super();
    }

    public TrinucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public TrinucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName,
            String name) {
        super(organism, uniqueName, name);
    }

}
