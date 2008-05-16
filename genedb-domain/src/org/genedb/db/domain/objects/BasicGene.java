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

    private List<Transcript> transcripts;
    private int featureId;
    private String systematicId;
    private String name;
    private String organism;
    private Chromosome chromosome;
    private List<String> products;
    private int fmin, fmax;
    private int strand;
    private List<String> synonyms;
    
    /**
     * Two BasicGenes are equal if they have the same systematicId.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicGene) {
            BasicGene other = (BasicGene) obj;
            return (this.systematicId.equals(other.systematicId));
        }
        else
            return false;
    }

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
        this.featureId = basis.featureId;
        this.systematicId = basis.systematicId;
        this.name = basis.name;
        this.organism = basis.organism;
        this.chromosome = basis.chromosome;
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
    public String displayName() {
        if (name != null)
            return name;
        else
            return systematicId;
    }

    public String getSystematicId() {
        return systematicId;
    }
    public void setSystematicId(String systematicId) {
        this.systematicId = systematicId;
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

    public List<String> getProducts() {
        return products;
    }
    public void setProducts(List<String> products) {
        this.products = products;
    }

    public List<Transcript> getTranscripts() {
        return transcripts;
    }
    public void addTranscript(Transcript transcript) {
        if (this.transcripts == null)
            this.transcripts = new ArrayList<Transcript> ();
        transcript.setGene(this);
        this.transcripts.add(transcript);
    }
    public void setTranscripts(List<Transcript> transcripts) {
        for (Transcript transcript: transcripts)
            transcript.setGene(this);

        this.transcripts = transcripts;
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

    public Chromosome getChromosome() {
        return chromosome;
    }
    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }
    
    public String getChromosomeName() {
        if (chromosome == null)
            return null;
        return chromosome.getName();
    }
}