package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;

import javax.persistence.Entity;

@Entity
@FeatureType(cv="sequence", term="protein_match")
public class ProteinMatch extends Match {
    ProteinMatch() {
        // empty
    }
}
