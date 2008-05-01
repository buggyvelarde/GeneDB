/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.domain.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author art
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Gene implements Serializable {

    private List<Transcript> transcripts = new ArrayList<Transcript>(1);
    private String geneFeatureId;
    private String systematicId;
    private String name;
    private List<String> synonyms;
    private List<String> orthologues;
    private List<String> paralogues;
    private List<String> clusters;
    private String previousSystematicId;
    private List<String> products;

    private String organism;
    private String reservedName;

    public String getSystematicId() {
        return systematicId;
    }

    public String getName() {
        return name;
    }

    public String getOrganism() {
        return organism;
    }

    public void setGeneFeatureId(String geneFeatureId) {
        this.geneFeatureId = geneFeatureId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public void setPreviousSystematicId(String previousSystematicId) {
        this.previousSystematicId = previousSystematicId;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public void setSystematicId(String systematicId) {
        this.systematicId = systematicId;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    public List<String> getOrthologues() {
        return orthologues;
    }

    public void setOrthologues(List<String> orthologues) {
        this.orthologues = orthologues;
    }

    public List<String> getParalogues() {
        return paralogues;
    }

    public void setParalogues(List<String> paralogues) {
        this.paralogues = paralogues;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setReservedName(String reservedName) {
        this.reservedName = reservedName;
    }

    public String getReservedName() {
        return reservedName;
    }

    public void addTranscript(Transcript transcript) {
        this.transcripts.add(transcript);
    }

    public List<Transcript> getTranscripts() {
        return transcripts;
    }

}
