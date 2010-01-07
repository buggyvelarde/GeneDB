package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@FeatureType(cv="sequence", term="tetranucleotide_repeat_microsatellite_feature")
public class TetraNucleotideRepeatMicrosatelliteFeature extends Microsatellite {

    TetraNucleotideRepeatMicrosatelliteFeature() {
        super();
    }

    public TetraNucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName,
            boolean analysis, boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public TetraNucleotideRepeatMicrosatelliteFeature(Organism organism, String uniqueName,
            String name) {
        super(organism, uniqueName, name);
    }

}
