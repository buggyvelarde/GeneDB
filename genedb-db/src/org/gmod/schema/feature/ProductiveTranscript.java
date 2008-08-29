package org.gmod.schema.feature;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * A transcript that may have products associated with it, i.e. an mRNA or
 * a PseudogenicTranscript.
 *
 * @author rh11
 */
@Entity
public abstract class ProductiveTranscript extends Transcript {

    ProductiveTranscript() {
        // empty
    }

    public ProductiveTranscript(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    /**
     * Return the uniqueName of the associated polypeptide.
     *
     * Indexed as <code>protein</code> in the Lucene index.
     *
     * @return the uniqueName of the associated polypeptide
     */
    @Transient
    @Field(name = "protein", store = Store.YES)
    public String getProteinUniqueName() {
        Feature protein = getProtein();
        if (protein == null) {
            return null;
        }
        return protein.getUniqueName();
    }

    /**
     * Get the associated polypeptide feature.
     *
     * A pseudogenic transcript may or may not have an associated
     * polypeptide.Every mRNA transcript should have one, though this
     * constraint cannot be enforced at the database level, so in practice
     * this method may return <code>null</code> even for an mRNA, which
     * indicates a curation issue with that transcript.
     *
     * @return
     */
    @Transient
    public Polypeptide getProtein() {
        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getSubjectFeature();
            if (feature instanceof Polypeptide) {
                return (Polypeptide) feature;
            }
        }
        return null;
    }

    @Transient
    public void setProtein(Polypeptide polypeptide) {
        if (getProtein() != null) {
            throw new RuntimeException(String.format(
                "Attempting to set a protein on transcript '%s' which already has one",
                getUniqueName()));
        }
        addFeatureRelationship(polypeptide, "sequence", "derives_from");
    }

    @Transient
    public List<String> getProducts() {
        Polypeptide protein = getProtein();
        if (protein == null) {
            return null;
        }
        return protein.getProducts();
    }

    @Transient
    @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        List<String> products = getProducts();
        if (products == null) {
            return null;
        }
        for (String product: getProducts()) {
            if (first) {
                first = false;
            } else {
                ret.append('\t');
            }
            ret.append(product);
        }
        return ret.toString();
    }

    @Override
    @Transient
    @Field(name = "colour", index = Index.UN_TOKENIZED, store = Store.YES)
    public Integer getColourId() {
        Polypeptide protein = getProtein();
        if (protein == null) {
            return null;
        }
        return protein.getColourId();
    }

}
