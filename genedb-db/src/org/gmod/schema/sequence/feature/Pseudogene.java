package org.gmod.schema.sequence.feature;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Entity
@DiscriminatorValue("423")
@Indexed
public class Pseudogene extends AbstractGene {
    @Transient
    public Collection<PseudogenicTranscript> getPseudogenicTranscripts() {
        Collection<PseudogenicTranscript> ret = new ArrayList<PseudogenicTranscript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getFeatureBySubjectId();
            if (transcript instanceof PseudogenicTranscript)
                ret.add((PseudogenicTranscript) transcript);
        }

        return ret;
    }

    @Override
    @Transient @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder products = new StringBuilder();

        boolean first = true;
        for (PseudogenicTranscript transcript : getPseudogenicTranscripts()) {
            if (first)
                first = false;
            else
                products.append('\t');
            products.append(transcript.getProductsAsTabSeparatedString());
        }
        return products.toString();
    }

}
