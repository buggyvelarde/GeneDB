package org.gmod.schema.sequence.feature;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Transient;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

/**
 * A {@link Gene} or a {@link Pseudogene}.
 *
 * @author rh11
 *
 */
public abstract class AbstractGene extends Feature {
    @Transient
    public Collection<Transcript> getTranscripts() {
        Collection<Transcript> ret = new ArrayList<Transcript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getFeatureBySubjectId();
            if (transcript instanceof Transcript)
                ret.add((Transcript) transcript);
        }

        return ret;
    }

    abstract public String getProductsAsTabSeparatedString();
}