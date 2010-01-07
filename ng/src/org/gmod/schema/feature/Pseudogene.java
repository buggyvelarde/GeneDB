package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.StrandedLocation;

import org.hibernate.search.annotations.Indexed;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv = "sequence", term = "pseudogene")
@Indexed
public class Pseudogene extends AbstractGene {

    Pseudogene() {
        // empty
    }

    public Pseudogene(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    Pseudogene(Organism organism, String uniqueName, String name) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        setName(name);
    }

    @Transient
    public Collection<PseudogenicTranscript> getPseudogenicTranscripts() {
        Collection<PseudogenicTranscript> ret = new ArrayList<PseudogenicTranscript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getSubjectFeature();
            if (transcript instanceof PseudogenicTranscript) {
                ret.add((PseudogenicTranscript) transcript);
            }
        }

        return ret;
    }

    public static Pseudogene make(Feature sourceFeature, StrandedLocation location, String uniqueName, Timestamp now) {
        Pseudogene gene = new Pseudogene(sourceFeature.getOrganism(), uniqueName, false, false, now);
        sourceFeature.addLocatedChild(gene, location);
        return gene;
    }

    public void addPseudogenicTranscript(PseudogenicTranscript transcript) {
        addFeatureRelationship(transcript, "relationship", "part_of");
    }
}
