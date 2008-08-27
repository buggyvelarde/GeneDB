package org.gmod.schema.feature;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;

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
     * feature (chromosome or contig).
     */
    @Transient
    @Override
    public String getResidues() {
        Feature parent = this.getRankZeroFeatureLoc().getSourceFeature();
        return parent.getResidues(this.getStart(), this.getStop());
    }
}
