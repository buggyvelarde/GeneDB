package org.gmod.schema.feature;

import org.genedb.db.analyzers.AllNamesAnalyzer;
import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv = "sequence", term = "transcript")
@Indexed
public class Transcript extends Region {

    private static Logger logger = Logger.getLogger(Transcript.class);
    @Transient
    protected AbstractGene gene;

    Transcript() {
        // empty
    }

    public Transcript(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    static <T extends Transcript> T construct(Class<T> transcriptClass, Organism organism,
            String uniqueName, String name) {
        try {
            return transcriptClass.getDeclaredConstructor(Organism.class, String.class, String.class)
            .newInstance(organism, uniqueName, name);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Internal error: failed to construct transcript", e);
        }
    }


    public Integer getColourId() {
        return null;
    }
    
    public AbstractGene getGene() {
        if (gene != null) {
            return gene;
        }

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature geneFeature = relation.getObjectFeature();
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
    
    @Transient
    protected Polypeptide polypeptide;
    
    @Transient
    public Polypeptide getPolypeptide() {
        if (polypeptide != null) {
            return polypeptide;
        }
        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature polypeptideFeature = relation.getSubjectFeature();
            logger.error(String.format("?? %s %s", polypeptideFeature.getUniqueName(), polypeptideFeature.getClass()));
            if (polypeptideFeature instanceof Polypeptide) {
            	polypeptide = (Polypeptide) polypeptideFeature;
                break;
            }
        }
        if (polypeptide == null) {
            logger.error(String.format("The transcript '%s' has no associated polypeptide", getUniqueName()));
            return null;
        }
        return polypeptide;
    }
    
    /**
     * Overrides to add the gene name to the names list.
     */
    @Override protected List<String> generateNamesList() {
    	List<String> names = super.generateNamesList();
    	AbstractGene gene = getGene();
        if (gene != null) {
    		names.add(gene.getUniqueName());
    	}
    	return names;
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
    	AbstractGene gene = getGene();
        if (gene != null) {
        	return gene.getUniqueName();
        }
        return null;
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
            Feature geneFeature = relation.getObjectFeature();
            if (geneFeature instanceof Gene) {
                foundGene = true;
                relation.setObjectFeature(gene);
                this.gene = gene;
                break;
            }
        }
        if (!foundGene) {
            logger.error(String.format("The transcript '%s' has no associated gene", getUniqueName()));
        }
    }

    @Transient
    public Collection<TranscriptRegion> getComponents() {
        return getComponents(TranscriptRegion.class);
    }

    @Transient
    public <T extends TranscriptRegion> SortedSet<T> getComponents(Class<T> regionClass) {
        SortedSet<T> components = new TreeSet<T>();

        for (FeatureRelationship relation : getFeatureRelationshipsForObjectId()) {
            Feature feature = relation.getSubjectFeature();
            if (regionClass.isInstance(feature)) {
                components.add(regionClass.cast(feature));
            }
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
            if (first) {
                first = false;
            } else {
                locs.append(',');
            }
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
            if (first) {
                first = false;
            } else {
                locs.append(", ");
            }
            locs.append(exon.getTraditionalLocAsString());
        }

        return locs.toString();
    }

    @Transient
    protected Class<? extends AbstractExon> getExonClass() {
        // This method is over-ridden in PseudogenicTranscript
        return Exon.class;
    }

    /**
     * Create a new exon, and add it to this transcript.
     *
     * @param exonUniqueName
     * @param fmin
     * @param fmax
     * @return the newly-created exon
     */
    public AbstractExon createExon(String exonUniqueName, int fmin, int fmax, Integer phase) {
        /*
         * NB This method is overridden in ProductiveTranscript.
         */
        return createRegion(getExonClass(), exonUniqueName, fmin, fmax, phase);
    }

    public <T extends UTR> T createUTR(Class<T> utrClass, String utrUniqueName, int fmin, int fmax) {
        return createRegion(utrClass, utrUniqueName, fmin, fmax, null /*phase*/);
    }


    /**
     * Add a TranscriptRegion to this transcript. If the new region is not contained in the boundaries
     * of this transcript, the transcript and gene boundaries are extended appropriately.
     *
     * @param <T>
     * @param componentClass the class of region to add
     * @param componentUniqueName the uniquename of the new region
     * @param fmin the <code>fmin</code>, relative to the primary source feature, of the new region.
     * @param fmax the <code>fmax</code>, relative to the primary source feature, of the new region
     * @param phase the <code>phase</code> of translation: 0, 1, 2, or <code>null</code>
     * @return
     */
    private <T extends TranscriptRegion> T createRegion(Class<T> componentClass, String componentUniqueName, int fmin, int fmax, Integer phase) {
        FeatureLoc ourLoc = getRankZeroFeatureLoc();
        if (fmin < ourLoc.getFmin()) {
            logger.debug(String.format("[%s] The %s start (%d) is before the transcript start (%d). Resetting transcript start",
                getUniqueName(), componentClass.getSimpleName(), fmin, ourLoc.getFmin()));
            lowerFminTo(fmin);
        }
        if (fmax > ourLoc.getFmax()) {
            logger.debug(String.format("[%s] The %s end (%d) is after the transcript end (%d). Resetting transcript end",
                getUniqueName(), componentClass.getSimpleName(), fmax, ourLoc.getFmax()));
            raiseFmaxTo(fmax);
        }

        int relativeFmin = fmin - getFmin();
        int relativeFmax = fmax - getFmin();

        T region = TranscriptRegion.construct(componentClass, this.getOrganism(), componentUniqueName);

        for (FeatureLoc featureLoc: getFeatureLocs()) {
            Feature sourceFeature = featureLoc.getSourceFeature();
            if (sourceFeature == null) {
                logger.error(String.format("Feature '%s' has a FeatureLoc (ID %d) with no source feature",
                    getUniqueName(), featureLoc.getFeatureLocId()));
            } else {
                sourceFeature.addLocatedChild(region, featureLoc.getFmin() + relativeFmin, featureLoc.getFmin() + relativeFmax,
                    featureLoc.getStrand(), phase, featureLoc.getLocGroup(), featureLoc.getRank());
            }
        }

        addRegion(region);
        return region;
    }

    /**
     * Move the lower bound of this feature leftwards. The new location is
     * specified relative to the primary source feature, but all <code>FeatureLoc</code>s
     * will be updated by the same relative amount.
     *
     * On a transcript, we also update the <code>fmin</code> of the associated gene,
     * if necessary.
     *
     * @param fmin the new <code>fmin</code> relative to the primary location
     */
    @Override
    public void lowerFminTo(int fmin) {
        super.lowerFminTo(fmin);
        getGene().lowerFminTo(fmin);
    }
    /**
     * Move the upper bound of this feature rightwards. The new location is
     * specified relative to the primary source feature, but all <code>FeatureLoc</code>s
     * will be updated by the same relative amount.
     *
     * On a transcript, we also update the <code>fmax</code> of the associated gene,
     * if necessary.
     *
     * @param fmax the new <code>fmax</code>, relative to the primary location
     */
    @Override
    public void raiseFmaxTo(int fmax) {
        super.raiseFmaxTo(fmax);
        getGene().raiseFmaxTo(fmax);
    }

    /**
     * Add an (already-created) region to this transcript.
     *
     * @param exon
     */
    void addRegion(TranscriptRegion region) {
        this.addFeatureRelationship(region, "relationship", "part_of");
    }
}
