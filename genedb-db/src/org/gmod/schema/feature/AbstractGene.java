package org.gmod.schema.feature;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * A {@link Gene} or a {@link Pseudogene}.
 *
 * @author rh11
 *
 */
@Entity
public abstract class AbstractGene extends Region {

    AbstractGene() {
        // empty
    }

    public AbstractGene(Organism organism, String uniqueName, boolean analysis, boolean obsolete,
            Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

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

    @Transient
    public abstract String getProductsAsTabSeparatedString();

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

    public <T extends Transcript> T makeTranscript(Class<T> transcriptClass, String uniqueName) {
        try {
            T transcript = transcriptClass.getDeclaredConstructor(Organism.class, String.class, String.class).newInstance(getOrganism(), uniqueName, null);
            this.getSourceFeature().addLocatedChild(transcript, this.getFmin(), this.getFmax(), this.getStrand(), 0);
            this.addFeatureRelationship(transcript, "relationship", "part_of");

            if (ProductiveTranscript.class.isAssignableFrom(transcriptClass)) {
                Polypeptide polypeptide = new Polypeptide(getOrganism(), String.format("%s:pep", uniqueName));
                ((ProductiveTranscript)transcript).setProtein(polypeptide);
            }

            return transcript;
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

    public synchronized FeatureSynonym addSynonym(String synonymString) {
        CvTerm synonymType = cvDao.findOrCreateCvTermByNameAndCvName("synonym", "genedb_synonym_type");
        return addSynonym(synonymType, synonymString);
    }

    protected FeatureSynonym addSynonym(CvTerm synonymType, String synonymString) {
        Synonym synonym = new Synonym(synonymType, synonymString, synonymString);
        FeatureSynonym featureSynonym = new FeatureSynonym(synonym, this, null /*pub*/ , true, false);
        this.addFeatureSynonym(featureSynonym);
        return featureSynonym;
    }

    /**
     * If this AbstractGene has a current synonym of the specified type,
     * the name and synonymSGML of that synonym are set to <code>synonymString</code>
     * and we return <code>true</code>.
     * If not, we do nothing and return <code>false</code>.
     * @param synonymType the type of synonym to look for
     * @param synonymString the string to which the synonym should be changed
     * @return whether or not a synonym was found
     */
    protected boolean setSynonymIfPresent(CvTerm synonymType, String synonymString) {
        for (FeatureSynonym featureSynonym: getFeatureSynonyms()) {
            if (!featureSynonym.isCurrent()) {
                continue;
            }

            Synonym thisSynonym = featureSynonym.getSynonym();
            CvTerm thisSynonymType = thisSynonym.getType();
            if (thisSynonymType.getCvTermId() == synonymType.getCvTermId()) {
                thisSynonym.setName(synonymString);
                thisSynonym.setSynonymSGML(synonymString);
                return true;
            }
        }
        return false;
    }

    public void setSystematicId(String systematicId) {
        CvTerm systematicIdType = cvDao.findOrCreateCvTermByNameAndCvName("systematic_id", "genedb_synonym_type");

        if (!setSynonymIfPresent(systematicIdType, systematicId)) {
            addSynonym(systematicIdType, systematicId);
        }
    }

    @Transient
    public void setTemporarySystematicId(String temporarySystematicId) {
        CvTerm temporarySystematicIdType = cvDao.findOrCreateCvTermByNameAndCvName("temporary_systematic_id", "genedb_synonym_type");
        if (!setSynonymIfPresent(temporarySystematicIdType, temporarySystematicId)) {
            addSynonym(temporarySystematicIdType, temporarySystematicId);
        }
    }
}
