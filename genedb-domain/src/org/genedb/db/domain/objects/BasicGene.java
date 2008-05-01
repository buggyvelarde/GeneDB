package org.genedb.db.domain.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the basic features of a gene. In practice, this means
 * those features that we keep in the Lucene index as well as the database. It's
 * populated by an instance of a BasicGeneService class.
 * 
 * @author rh11
 *
 */
public class BasicGene {

    private List<Transcript> transcripts = new ArrayList<Transcript>(1);
    private String geneFeatureId;
    private String systematicId;
    private String previousSystematicId;
    private String name;
    private String organism;
    private List<String> products;

    /**
     * Create a new, empty, BasicGene.
     */
    public BasicGene() { }
    /**
     * Copy an existing BasicGene.
     * 
     * @param basis The object to copy
     */
    protected BasicGene (BasicGene basis) {
        this.transcripts = basis.transcripts;
        this.geneFeatureId = basis.geneFeatureId;
        this.systematicId = basis.systematicId;
        this.previousSystematicId = basis.previousSystematicId;
        this.name = basis.name;
        this.organism = basis.organism;
        this.products = basis.products;
    }

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

    public void setSystematicId(String systematicId) {
        this.systematicId = systematicId;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

    public void addTranscript(Transcript transcript) {
        this.transcripts.add(transcript);
    }

    public List<Transcript> getTranscripts() {
        return transcripts;
    }

}