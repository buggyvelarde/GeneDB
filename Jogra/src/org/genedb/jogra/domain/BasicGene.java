package org.genedb.jogra.domain;

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

    //private List<Transcript> transcripts;
    private int featureId;
    private String uniqueName;
    private String name;
    private String organism;
    private String topLevelFeature;
    private List<String> products;
    private int fmin, fmax;
    private int strand;
    private List<String> synonyms;

    /**
     * Two BasicGenes are equal if they have the same uniqueName.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicGene) {
            BasicGene other = (BasicGene) obj;
            return (this.uniqueName.equals(other.uniqueName));
        }
        return false;
    }

    /**
     * Create a new, empty, BasicGene.
     */
    public BasicGene() {
        // Default constructor
    }
    /**
     * Copy an existing BasicGene.
     *
     * @param basis The object to copy
     */
    protected BasicGene (BasicGene basis) {
        //this.transcripts = basis.transcripts;
        this.featureId = basis.featureId;
        this.uniqueName = basis.uniqueName;
        this.name = basis.name;
        this.organism = basis.organism;
        //this.chromosome = basis.chromosome;
        this.products = basis.products;
        this.fmin = basis.fmin;
        this.fmax = basis.fmax;
        this.strand = basis.strand;
        this.synonyms = basis.synonyms;
    }

    /**
     * The display name is the name if there is one, or the systematic ID if not.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (name != null) {
            return name;
        }
        return uniqueName;
    }

    public String getUniqueName() {
        return uniqueName;
    }
    public void setUniqueName(String systematicId) {
        this.uniqueName = systematicId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getOrganism() {
        return organism;
    }
    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public int getFmin() {
        return fmin;
    }
    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

    public int getFmax() {
        return fmax;
    }
    public void setFmax(int fmax) {
        this.fmax = fmax;
    }

    public int getFeatureId() {
        return featureId;
    }
    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public int getStrand() {
        return strand;
    }
    public void setStrand(int strand) {
        this.strand = strand;
    }


}