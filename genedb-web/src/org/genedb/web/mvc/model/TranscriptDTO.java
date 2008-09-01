package org.genedb.web.mvc.model;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureProp;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Transient;

public class TranscriptDTO implements Serializable {

    private transient Logger logger = Logger.getLogger(TranscriptDTO.class);

    private String uniqueName;
    private String properName;
    private List<String> synonyms;
    private List<String> obsoleteNames;
    private List<String> products;
    private String typeDescription;
    private String topLevelFeatureType;
    private String topLevelFeatureName;
    private boolean alternateTranscript;
    private String geneName;
    private List<String> notes;
    private List<String> comments;
    private boolean pseudo;



    public void populate(Transcript transcript) {
        AbstractGene gene = transcript.getGene();
        Polypeptide polypeptide = null;
        if (transcript instanceof ProductiveTranscript) {
            polypeptide = ((ProductiveTranscript)transcript).getProtein();
            if (transcript instanceof PseudogenicTranscript) {
                pseudo = true;
            } else {
                pseudo = false;
            }
        }

        populateNames(transcript);
        populateParentDetails();
        populateMisc(transcript);

        if (polypeptide != null) {
            populateFromFeatureProps(polypeptide);
        }

    }



    private void populateMisc(Transcript transcript) {
        String type = transcript.getType().getName();
        typeDescription = type;
        if ("mRNA".equals(type)) {
            typeDescription = "Protein coding gene";
        } else {
            if ("pseudogenic_transcript".equals(type)) {
                typeDescription = "Pseudogene";
            }
        }
    }



    private void populateParentDetails() {
        // TODO Auto-generated method stub

    }



    private void populateFromFeatureProps(Polypeptide polypeptide) {

        if (polypeptide != null) {
            this.notes = stringListFromFeaturePropList(polypeptide, "feature_property", "comment");
            this.comments = stringListFromFeaturePropList(polypeptide, "genedb_misc", "curation");
        }

    }



    private List<String> stringListFromFeaturePropList(Polypeptide polypeptide, String cvName, String cvTermName) {
        List<String> ret = new ArrayList<String>();
        List<FeatureProp> featurePropNotes = polypeptide.getFeaturePropsFilteredByCvNameAndTermName(cvName, cvTermName);
        for (FeatureProp featureProp : featurePropNotes) {
            ret.add(featureProp.getValue());
        }
        logger.debug(String.format("Got '%d' results for filtering featureprops for '%s' in '%s'", ret.size(), cvTermName, cvName));
        if (ret.size() > 0) {
            return ret;
        }
        return Collections.emptyList();
    }



    private void populateNames(Transcript transcript) {
        this.uniqueName = transcript.getUniqueName();

    }



    public String getUniqueName() {
        return uniqueName;
    }



    public String getProperName() {
        return properName;
    }



    public List<String> getSynonyms() {
        return synonyms;
    }



    public List<String> getObsoleteNames() {
        return obsoleteNames;
    }



    public List<String> getProducts() {
        return products;
    }



    public String getTypeDescription() {
        return typeDescription;
    }



    public String getTopLevelFeatureType() {
        return topLevelFeatureType;
    }



    public String getTopLevelFeatureName() {
        return topLevelFeatureName;
    }



    public boolean isAlternateTranscript() {
        return alternateTranscript;
    }



    public String getGeneName() {
        return geneName;
    }



    public List<String> getNotes() {
        return notes;
    }



    public List<String> getComments() {
        return comments;
    }



    public boolean isPseudo() {
        return pseudo;
    }

}
