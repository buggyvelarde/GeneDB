package org.gmod.schema.sequence.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

@Entity
@DiscriminatorValue("191")
public class Polypeptide extends Feature {
    private static Logger logger = Logger.getLogger(Polypeptide.class);
    @Transient
    private Transcript transcript = null;
    
    public Transcript getTranscript() {
        if (transcript != null)
            return transcript;

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature transcriptFeature = relation.getFeatureByObjectId();
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
}
