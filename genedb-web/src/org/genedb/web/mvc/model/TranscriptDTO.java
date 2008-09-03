package org.genedb.web.mvc.model;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Synonym;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.jsp.JspWriter;

public class TranscriptDTO implements Serializable {

    private transient Logger logger = Logger.getLogger(TranscriptDTO.class);

    private String uniqueName;
    private String properName;
    private List<String> synonyms;
    private List<String> obsoleteNames;
    private List<String> products;
    private String typeDescription;
    private String topLevelFeatureType;
    private String topLevelFeatureDisplayName;
    private String geneName;
    private List<String> notes;
    private List<String> comments;
    private boolean pseudo;
    private boolean anAlternateTranscript;
    private String location;




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

        if (gene.getTranscripts().size()>1) {
            anAlternateTranscript = true;
        }

        populateNames(transcript, gene);
        populateParentDetails(gene);
        populateMisc(transcript);

        if (polypeptide != null) {
            populateFromFeatureProps(polypeptide);
            populateFromFeatureCvTerms(polypeptide);
        }

    }



    private void populateFromFeatureCvTerms(Polypeptide polypeptide) {
        Assert.notNull(polypeptide);
        this.products = polypeptide.getProducts();
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

        this.location = transcript.getExonLocsTraditional();
    }



    private void populateParentDetails(AbstractGene gene) {
        Feature topLevelFeature = gene.getRankZeroFeatureLoc().getSourceFeature();
        topLevelFeatureType = topLevelFeature.getType().getName();
        topLevelFeatureDisplayName = topLevelFeature.getDisplayName();
    }



    private void populateFromFeatureProps(Polypeptide polypeptide) {
        Assert.notNull(polypeptide);
        this.notes = stringListFromFeaturePropList(polypeptide, "feature_property", "comment");
        this.comments = stringListFromFeaturePropList(polypeptide, "genedb_misc", "curation");
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



    private void populateNames(Transcript transcript, AbstractGene gene) {
        this.uniqueName = transcript.getUniqueName();
        this.geneName = gene.getDisplayName();
        if (transcript.getName() != null && !transcript.getName().equals(uniqueName)) {
            this.properName = transcript.getName();
        }

        Collection<FeatureSynonym> featureSynonyms = gene.getFeatureSynonyms();

        obsoleteNames = findFromSynonymsByType(featureSynonyms, "obsolete_name");
        synonyms = findFromSynonymsByType(featureSynonyms, "synonym");
    }



    private List<String> findFromSynonymsByType(Collection<FeatureSynonym> synonyms, String typeName) {
        List<String> filtered = new ArrayList<String>();
        for (FeatureSynonym featSynonym : synonyms) {
            Synonym synonym = featSynonym.getSynonym();
            if (typeName.equals(synonym.getType().getName())) {
                filtered.add(synonym.getName());
            }
        }
        if (filtered.size() > 0) {
            return filtered;
        }
        return null;
    }


    public String getUniqueName() {
        return uniqueName;
    }



    public String getProperName() {
        return properName;
    }



    public List<String> getSynonyms() {
        return listOrEmptyList(synonyms);
    }



    public List<String> getObsoleteNames() {
        return listOrEmptyList(obsoleteNames);
    }



    public List<String> getProducts() {
        return listOrEmptyList(products);
    }



    private <T> List<T> listOrEmptyList(List<T> list) {
        if (list == null || list.size() == 0) {
            return Collections.emptyList();
        }
        return list;
    }



    public String getTypeDescription() {
        return typeDescription;
    }



    public String getTopLevelFeatureType() {
        return topLevelFeatureType;
    }



    public String getTopLevelFeatureDisplayName() {
        return topLevelFeatureDisplayName;
    }



    public boolean isAnAlternateTranscript() {
        return anAlternateTranscript;
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



    public String getLocation() {
        return location;
    }

}
