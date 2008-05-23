package org.gmod.schema.sequence.feature;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

public abstract class Transcript extends Feature {

    private static Logger logger = Logger.getLogger(MRNA.class);
    @Transient
    private AbstractGene gene;

    public abstract Integer getColourId();

    public Transcript() {
        super();
    }

    public Transcript(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public AbstractGene getGene() {
        if (gene != null)
            return gene;
        
        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature geneFeature = relation.getFeatureByObjectId();
            if (geneFeature instanceof Gene) {
                gene = (AbstractGene) geneFeature;
                break;
            }
        }
        if (gene == null) {
            logger.error(String.format("The mRNA transcript '%s' has no associated gene", getUniqueName()));
            return null;
        }
        return gene;
    }

    /**
     * Get the uniqueName of the gene associated to this transcript.
     * Equivalent to <code>getGene().getUniqueName()</code>.
     * 
     * Indexed as <code>gene</code> in the Lucene index.
     * 
     * @return
     */
    @Transient
    @Field(name = "gene", index = Index.UN_TOKENIZED, store = Store.YES)
    public String getGeneUniqueName() {
        return getGene().getUniqueName();
    }

    /**
     * Change the gene associated with this transcript.
     * Assumes that there already is an associated gene.
     * 
     * @param gene
     */
    public void setGene(AbstractGene gene) {
        boolean foundGene = false;
        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature geneFeature = relation.getFeatureByObjectId();
            if (geneFeature instanceof Gene) {
                foundGene = true;
                relation.setFeatureByObjectId(gene);
                this.gene = gene;
                break;
            }
        }
        if (!foundGene)
            logger.error(String.format("The mRNA transcript '%s' has no associated gene", getUniqueName()));
    }

    @Transient
    Collection<Exon> getExons() {
        List<Exon> exons = new ArrayList<Exon>();
        
        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getFeatureBySubjectId();
            if (feature instanceof Exon)
                exons.add((Exon) feature);
        }
        
        return exons;
    }

    @Transient
    @Field(name = "exonlocs", store = Store.YES)
    public String getExonLocs() {
        StringBuilder locs = new StringBuilder();
    
        boolean first = true;
        for (AbstractExon exon : getExons()) {
            if (first)
                first = false;
            else
                locs.append(',');
            locs.append(exon.getLocAsString());
        }
        
        return locs.toString();
    }

}