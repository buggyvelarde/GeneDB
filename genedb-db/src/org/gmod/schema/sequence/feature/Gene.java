package org.gmod.schema.sequence.feature;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Entity
@DiscriminatorValue("792")
@Indexed
public class Gene extends AbstractGene {
    @Transient
    public Collection<MRNA> getCodingTranscripts() {
        Collection<MRNA> ret = new ArrayList<MRNA>();
        
        for (Transcript transcript : getTranscripts()) {
            if (transcript instanceof MRNA)
                ret.add((MRNA) transcript);
        }
        
        return ret;
    }

    @Transient @Field(name = "protein", store = Store.YES)
    public String getProteinUniqueNamesTabSeparated() {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (MRNA transcript: getCodingTranscripts()) {
            if (first)
                first = false;
            else
                ret.append('\t');
            ret.append(transcript.getProteinUniqueName());
        }
        return ret.toString();
    }

    @Transient @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder products = new StringBuilder();

        boolean first = true;
        for (MRNA transcript : getCodingTranscripts()) {
            if (first)
                first = false;
            else
                products.append('\t');
            products.append(transcript.getProductsAsTabSeparatedString());
        }
        return products.toString();
    }
}
