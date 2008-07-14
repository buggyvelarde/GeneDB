package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="pseudogene")
@Indexed
public class Pseudogene extends AbstractGene {
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

    @Override
    @Transient @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder products = new StringBuilder();

        boolean first = true;
        for (PseudogenicTranscript transcript : getPseudogenicTranscripts()) {
            if (first) {
                first = false;
            } else {
                products.append('\t');
            }
            products.append(transcript.getProductsAsTabSeparatedString());
        }
        return products.toString();
    }

}
