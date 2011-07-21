package org.gmod.schema.feature;

import org.genedb.db.analyzers.AllNamesAnalyzer;
import org.genedb.db.loading.EmblLocation;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * A {@link Gene} or a {@link Pseudogene}.
 *
 * @author rh11
 *
 */
@Entity
public abstract class AbstractGene extends TopLevelFeature {
    private static final Logger logger = Logger.getLogger(AbstractGene.class);

    AbstractGene() {
        // empty
    }

    public AbstractGene(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    
    @Transient
    @Field(name = "gene", index = Index.UN_TOKENIZED, store = Store.YES)
    public String getGeneUniqueName() {
    	return getUniqueName();
    }
    
    private transient Transcript firstTranscripts;


    /**
     * Get a collection of this gene's transcripts.
     * @return a collection of this gene's transcripts
     */
    @Transient
    public Collection<Transcript> getTranscripts() {
        Collection<Transcript> ret = new ArrayList<Transcript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getSubjectFeature();
            if (transcript instanceof Transcript) {
                ret.add((Transcript) transcript);
            }
        }

        return ret;
    }
    
    /**
     * Get a collection of this gene's transcripts. that aren't obsolete.
     * @return a collection of this gene's transcripts that aren't obsolete
     */
    @Transient
    public Collection<Transcript> getNonObsoleteTranscripts() {
        Collection<Transcript> ret = new ArrayList<Transcript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getSubjectFeature();
            if (transcript instanceof Transcript && transcript.isObsolete() == false) {
                ret.add((Transcript) transcript);
            }
        }

        return ret;
    }
    
    @Transient
    @Field(name = "alternateTranscriptNumber", index = Index.UN_TOKENIZED, store = Store.YES)
    public int alternateTranscriptNumber() {
    	return getNonObsoleteTranscripts().size();
    }
    
    @Transient
    @Field(name = "alternateTranscripts", index = Index.UN_TOKENIZED, store = Store.YES)
    public String alternateTranscripts() {
    	StringBuffer alternateTranscripts = new StringBuffer();
    	for (Transcript t : getNonObsoleteTranscripts()) {
    		alternateTranscripts.append(t.getUniqueName());
    	}
    	return alternateTranscripts.toString();
    }
    
    @Transient
    @Analyzer(impl = AllNamesAnalyzer.class)
    @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsSpaceSeparatedString() {
    	if (getFirstTranscript() != null) {
    		return getFirstTranscript().getProductsAsSpaceSeparatedString();
    	}
    	return null;
    }

    @Transient
    public Transcript getFirstTranscript() {
        if (firstTranscripts != null) {
            return firstTranscripts;
        }

        List<Transcript> temp = new ArrayList<Transcript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getSubjectFeature();
            if (transcript instanceof Transcript) {
                temp.add((Transcript) transcript);
            }
        }

        //find first item in sorted list
        if (temp.size() > 1){
            Transcript tempTanscript = temp.get(0);
            for(Transcript t : temp){
                if (tempTanscript.getUniqueName().compareTo(t.getUniqueName()) > 0){
                    tempTanscript = t;
                }
            }
            firstTranscripts = tempTanscript;
        }else if (temp.size() == 1){
            firstTranscripts = temp.get(0);
        }
        return firstTranscripts;
    }

    /**
     * Is this a pseudogene?
     * @return <code>true</code> if this is a pseudogene, or <code>false</code> if not
     */
    @Transient
    public boolean isPseudo() {
        return (this instanceof Pseudogene);
    }

    /**
     * Rather than returning the residues stored in the database, this method
     * extracts the appropriate subsequence of the residues of the source
     * feature (chromosome, supercontig or contig).
     */
    @Transient
    @Override
    public String getResidues() {
        Feature parent = this.getRankZeroFeatureLoc().getSourceFeature();
        return parent.getResidues(this.getStart(), this.getStop());
    }

    public static <T extends AbstractGene> T make(Class<T> geneClass, Organism organism, String uniqueName, String name) {
        try {
            return geneClass.getDeclaredConstructor(Organism.class, String.class, String.class).newInstance(organism, uniqueName, name);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Internal error: failed to construct gene", e);
        }
    }

    /**
     * Create a transcript for this gene, which is assumed to be newly-created
     * and not to have a transcript yet.
     *
     * @param <T>
     * @param transcriptClass the class of the transcript to create
     * @param transcriptUniqueName the uniqueName of the transcript to create
     * @param fmin
     * @param fmax
     * @return
     */
    public <T extends Transcript> T makeTranscript(Class<T> transcriptClass, String transcriptUniqueName,
            int fmin, int fmax) {
            T transcript = makeTranscript(transcriptClass, transcriptUniqueName, fmin, fmax, null, null); //don't need to supply the gene and EmblLocation for transcripts located on normal topLevel features
            return transcript;
    }

    /**
     * Create a transcript for this gene, which is assumed to be newly-created
     * and not to have a transcript yet.
     *
     *For transcripts located on topLevel genes the gene and EmblLocation objects
     *are required for featureLocing
     *
     * @param <T>
     * @param transcriptClass the class of the transcript to create
     * @param transcriptUniqueName the uniqueName of the transcript to create
     * @param fmin
     * @param fmax
     * @param gene the gene feature if it is a topLevel gene (else null)
     * @param location the EmblLocation object if the gene is topLevel (else null)
     * @return
     */

    public <T extends Transcript> T makeTranscript(Class<T> transcriptClass, String transcriptUniqueName,
                int fmin, int fmax, AbstractGene gene, EmblLocation location) {

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        T transcript = Transcript.construct(transcriptClass, getOrganism(), transcriptUniqueName, null);
        session.persist(transcript);

        int relativeFmin = 0;
        int relativeFmax = 0;

        if (gene != null && gene.isTopLevelFeature()) { //gene is topLevel so featureLoc transcript onto gene
            logger.trace(String.format("Creating transcript '%s' for gene '%s' at locations %d..%d on a topLevel gene",
                    transcriptUniqueName, getUniqueName(), fmin, fmax));
            gene.addLocatedChild(transcript, fmin, fmax, location.getStrand(), null /*phase*/);
        }
        else { //normal genes on normal topLevel features
            logger.trace(String.format("Creating transcript '%s' for gene '%s' at locations %d..%d (gene locations %d..%d)",
                    transcriptUniqueName, getUniqueName(), fmin, fmax, getFmin(), getFmax()));

            if (fmax < fmin) {
                throw new IllegalArgumentException(String.format("fmax (%d) < fmin (%d)", fmax, fmin));
            }

            relativeFmin = fmin - this.getFmin();
            if (relativeFmin < 0) {
                logger.trace(String.format("Start of transcript (%d) is before start of gene (%d). Resetting gene start",
                        fmin, this.getFmin()));
                this.lowerFminTo(fmin);
            }

            relativeFmax = fmax - this.getFmin();
            if (fmax > this.getFmax()) {
                logger.trace(String.format("End of transcript (%d) is after end of gene (%d). Resetting gene end",
                        fmax, this.getFmax()));
                this.raiseFmaxTo(fmax);
            }

            for (FeatureLoc featureLoc: getFeatureLocs()) {
                featureLoc.getSourceFeature().addLocatedChild(transcript, featureLoc.getFmin() + relativeFmin,
                        featureLoc.getFmin() + relativeFmax,
                        featureLoc.getStrand(), null /*phase*/, featureLoc.getLocGroup(), featureLoc.getRank());
            }
        }
        this.addFeatureRelationship(transcript, "relationship", "part_of");

        if (ProductiveTranscript.class.isAssignableFrom(transcriptClass)) {
            String polypeptideUniqueName;
            if (transcriptUniqueName.endsWith(":mRNA")) {
                polypeptideUniqueName = String.format("%s:pep", transcriptUniqueName.substring(0, transcriptUniqueName.length() - 5));
            } else {
                polypeptideUniqueName = String.format("%s:pep", transcriptUniqueName);
            }
            logger.debug(String.format("Creating polypeptide '%s' for transcript '%s'",
                polypeptideUniqueName, getUniqueName()));
            Polypeptide polypeptide = new Polypeptide(getOrganism(), polypeptideUniqueName);
            session.persist(polypeptide);
            for (FeatureLoc featureLoc: transcript.getFeatureLocs()) {
                featureLoc.getSourceFeature().addLocatedChild(polypeptide, featureLoc.getFmin(), featureLoc.getFmax(),
                    featureLoc.getStrand(), null /*phase*/, featureLoc.getLocGroup(), featureLoc.getRank());
            }
            ((ProductiveTranscript)transcript).setProtein(polypeptide);
        }
        
        return transcript;
    }
}
