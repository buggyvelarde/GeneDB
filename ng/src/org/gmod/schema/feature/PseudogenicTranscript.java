package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
@FeatureType(cv = "sequence", term = "pseudogenic_transcript")
@Indexed
public class PseudogenicTranscript extends ProductiveTranscript {
    PseudogenicTranscript() {
        // empty
    }

    public PseudogenicTranscript(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    PseudogenicTranscript(Organism organism, String uniqueName, String name) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        setName(name);
    }

    @Override
    protected Class<? extends AbstractExon> getExonClass() {
        return PseudogenicExon.class;
    }

}
