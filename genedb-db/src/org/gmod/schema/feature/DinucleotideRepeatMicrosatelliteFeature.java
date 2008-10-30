package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="dinucleotide_repeat_microsatellite_feature")
public class DinucleotideRepeatMicrosatelliteFeature extends Microsatellite {

    DinucleotideRepeatMicrosatelliteFeature() {
        super();
    }

    public DinucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public DinucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName, String name) {
        super(organism, uniqueName, name);
    }

}
