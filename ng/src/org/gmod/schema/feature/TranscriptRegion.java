package org.gmod.schema.feature;


import org.apache.log4j.Logger;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * SO:0000833 (but we don't use it directly, which is why it's an
 * abstract class).
 *
 * @author rh11
 */
@Entity
public abstract class TranscriptRegion extends Region {
	
	private static Logger logger = Logger.getLogger(TranscriptRegion.class);
	
	@Transient
    private AbstractGene gene;
	
	@Transient
    private Transcript transcript;
	
    TranscriptRegion() {
        // empty
    }

    public TranscriptRegion(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    public TranscriptRegion(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    static <T extends TranscriptRegion> T construct(Class<T> regionClass, Organism organism, String uniqueName) {
        try {
            return regionClass.getConstructor(Organism.class, String.class).newInstance(organism, uniqueName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Internal error: failed to instantiate region", e);
        }
    }
    
    public Transcript getTranscript() {
        if (transcript != null) {
            return transcript;
        }

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature transcriptFeature = relation.getObjectFeature();
            if (transcriptFeature instanceof Transcript) {
                transcript = (Transcript) transcriptFeature;
                break;
            }
        }
        if (transcript == null) {
            logger.error(String.format("The polypeptide '%s' has no associated transcript", getUniqueName()));
            return null;
        }
        return transcript;
    }

    public AbstractGene getGene() {
        if (gene != null) {
            return gene;
        }

        Transcript transcript = getTranscript();
        if (transcript == null) {
            return null;
        }

        gene = transcript.getGene();
        return gene;
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
    


}
