package org.gmod.schema.sequence.feature;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

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
    @Transient
    public Collection<Transcript> getTranscripts() {
        Collection<Transcript> ret = new ArrayList<Transcript>();

        for (FeatureRelationship relationship : this.getFeatureRelationshipsForObjectId()) {
            Feature transcript = relationship.getFeatureBySubjectId();
            if (transcript instanceof Transcript) {
                ret.add((Transcript) transcript);
            }
        }

        return ret;
    }

    @Transient
    abstract public String getProductsAsTabSeparatedString();

    @Transient
    public boolean isPseudo() {
        return (this instanceof Pseudogene);
    }

    /**
     * Rather than returning the residues stored in the database,
     * this method extracts the appropriate subsequence of the
     * residues of the source feature (chromosome or contig).
     */
    @Transient @Override
    public byte[] getResidues() {
        byte[] sequence = null;
        Feature parent = this.getRankZeroFeatureLoc().getFeatureBySrcFeatureId();
        sequence = parent.getResidues(this.getStart(), this.getStop());
        return sequence;
    }
}