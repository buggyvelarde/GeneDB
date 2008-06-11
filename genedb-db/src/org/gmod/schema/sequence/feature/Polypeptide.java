package org.gmod.schema.sequence.feature;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

@Entity
@DiscriminatorValue("191")
public class Polypeptide extends Feature {
    private static Logger logger = Logger.getLogger(Polypeptide.class);
    @Transient
    private Transcript transcript = null;

    public Transcript getTranscript() {
        if (transcript != null)
            return transcript;

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature transcriptFeature = relation.getFeatureByObjectId();
            if (transcriptFeature instanceof Transcript) {
                transcript = (Transcript) transcriptFeature;
                break;
            }
        }
        if (transcript == null) {
            logger.error(String.format("The polypeptide '%s' has no associated transcript", getUniqueName()));
            return null;
        }
        return transcript;
    }

    @Transient
    public AbstractGene getGene() {
        Transcript transcript = getTranscript();

        for (FeatureRelationship relation : transcript.getFeatureRelationshipsForSubjectId()) {
            Feature geneFeature = relation.getFeatureByObjectId();
            if(geneFeature instanceof AbstractGene) {
                return (AbstractGene)geneFeature;
            }
        }

        return null;
    }

    @Transient
    public List<String> getProducts() {
        List<String> products = new ArrayList<String>();
        for (FeatureCvTerm featureCvTerm : this.getFeatureCvTerms()) {
            if (featureCvTerm.getCvTerm().getCv().getName().equals("genedb_products")) {
                products.add(featureCvTerm.getCvTerm().getName());
            }
        }
        return products;
    }

    /**
     * Get the ID number of the colour associated with this polypeptide.
     * It is often unassigned, in which case <code>null</code> is returned.
     *
     * @return
     */
    @Transient
    public Integer getColourId() {

        /* Sometimes there is no colour property at all,
        and sometimes there is a colour property with a null value.

        I don't know why this inconsistency exists. —rh11 */

        for (FeatureProp featureProp : this.getFeatureProps()) {
            if (featureProp.getCvTerm().getName().equals("colour")) {
                String colourString = featureProp.getValue();
                if (colourString == null)
                    return null;

                return new Integer(colourString);
            }
        }

        return null;
    }
}
