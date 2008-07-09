package org.gmod.schema.sequence.feature;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

@Entity
public abstract class Transcript extends Region {

    private static Logger logger = Logger.getLogger(Transcript.class);
    @Transient
    private AbstractGene gene;

    public abstract Integer getColourId();

    public AbstractGene getGene() {
        if (gene != null)
            return gene;

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature geneFeature = relation.getFeatureByObjectId();
            if (geneFeature instanceof AbstractGene) {
                gene = (AbstractGene) geneFeature;
                break;
            }
        }
        if (gene == null) {
            logger.error(String.format("The transcript '%s' has no associated gene", getUniqueName()));
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
    public Collection<TranscriptRegion> getComponents() {
        return getComponents(TranscriptRegion.class);
    }

    @Transient
    public <T extends TranscriptRegion> SortedSet<T> getComponents(Class<T> clazz) {
        SortedSet<T> components = new TreeSet<T>();

        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getFeatureBySubjectId();
            if (clazz.isInstance(feature))
                components.add(clazz.cast(feature));
        }

        return components;
    }

    @Transient
    public SortedSet<AbstractExon> getExons() {
        return getComponents(AbstractExon.class);
    }

    /**
     * Get the component locations, as a comma-separated string.
     * Includes UTRs as well as exons. This is stored in the Lucene index,
     * for use by the chromosome browser.
     *
     * @return
     */
    @Transient
    @Field(name = "locs", store = Store.YES)
    public String getExonLocs() {
        StringBuilder locs = new StringBuilder();

        boolean first = true;
        for (TranscriptRegion component : getComponents(TranscriptRegion.class)) {
            if (first)
                first = false;
            else
                locs.append(',');
            if (!(component instanceof AbstractExon)) {
                locs.append(component.getClass().getSimpleName());
                locs.append(':');
            }
            locs.append(component.getLocAsString());
        }

        return locs.toString();
    }

    /**
     * Get the exon locations in a form suitable for displaying
     * to the end-user, in traditional coordinates and separated
     * by a comma and a space.
     *
     * @return
     */
    @Transient
    public String getExonLocsTraditional() {
        StringBuilder locs = new StringBuilder();

        boolean first = true;
        for (AbstractExon exon : getExons()) {
            if (first)
                first = false;
            else
                locs.append(", ");
            locs.append(exon.getTraditionalLocAsString());
        }

        return locs.toString();
    }

}