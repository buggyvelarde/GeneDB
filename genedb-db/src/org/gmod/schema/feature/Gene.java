package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="gene")
@Indexed
public class Gene extends AbstractGene {

    Gene () {
        // empty
    }

    public Gene(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    Gene(Organism organism, String uniqueName, String name) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
        setName(name);
    }

    @Transient
    public Collection<MRNA> getCodingTranscripts() {
        Collection<MRNA> ret = new ArrayList<MRNA>();

        for (Transcript transcript : getTranscripts()) {
            if (transcript instanceof MRNA) {
                ret.add((MRNA) transcript);
            }
        }

        return ret;
    }

    @Transient @Field(name = "protein", store = Store.YES)
    public String getProteinUniqueNamesTabSeparated() {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (ProductiveTranscript transcript: getCodingTranscripts()) {
            if (first) {
                first = false;
            } else {
                ret.append('\t');
            }
            ret.append(transcript.getProteinUniqueName());
        }
        return ret.toString();
    }

    public void addTranscript(Transcript transcript) {
        addFeatureRelationship(transcript, "relationship", "part_of");
    }

}
