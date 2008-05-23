package org.gmod.schema.sequence.feature;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@DiscriminatorValue("321")
@Indexed
public class MRNA extends Transcript {
    
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
        if (protein == null)
            return null;
        return protein.getUniqueName();
    }
    
    /**
     * Get the associated polypeptide feature.
     * 
     * Every transcript should have one, though this constraint
     * cannot be enforced at the database level: i.e. in practice
     * this method may return <code>null</code>, which indicates
     * a curation issue with the transcript.
     * 
     * @return
     */
    @Transient
    public Feature getProtein() {
        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getFeatureBySubjectId();
            if (feature.getCvTerm().getName().equals("polypeptide"))
                return feature;
        }
        return null;
    }

    @Transient
    @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsTabSeparatedString() {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (String product: getProducts()) {
            if (first)
                first = false;
            else
                ret.append('\t');
            ret.append(product);
        }
        return ret.toString();
    }
    
    @Transient
    public List<String> getProducts() {
        List<String> products = new ArrayList<String>();
        Feature protein = getProtein();
        if (protein == null)
            return null;

        for (FeatureCvTerm featureCvTerm : protein.getFeatureCvTerms()) {
            if (featureCvTerm.getCvTerm().getCv().getName().equals("genedb_products")) {
                products.add(featureCvTerm.getCvTerm().getName());
            }
        }
        return products;
    }

    /**
     * Get the ID number of the colour associated with this transcript's polypeptide.
     * It is often unassigned, in which case <code>null</code> is returned.
     * 
     * @return
     */
    @Override
    @Transient @Field(name = "colour", index = Index.UN_TOKENIZED, store = Store.YES)
    public Integer getColourId() {
        /* Sometimes there is no colour property at all,
           and sometimes there is a colour property with a null value.
        
           I don't know why this inconsistency exists. —rh11 */

        for (FeatureProp featureProp : getProtein().getFeatureProps()) {
            if (featureProp.getCvTerm().getName().equals("colour")) {
                String colourString = featureProp.getValue();
                if (colourString == null)
                    return null;
                
                return Integer.parseInt(colourString);
            }
        }
        
        return null;
    }
}
