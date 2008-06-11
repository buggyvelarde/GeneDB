package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.gmod.schema.sequence.FeatureLoc;

/**
 * An {@link Exon} or {@link PseudogenicExon}.
 *
 * @author rh11
 *
 */
@Entity
public abstract class AbstractExon extends TranscriptComponent {

    @Transient
    protected String getLocAsString() {
        FeatureLoc featureLoc = getRankZeroFeatureLoc();
        int min = featureLoc.getFmin();
        int max = featureLoc.getFmax();

        short strand = featureLoc.getStrand();
        if (strand == -1)
            return "(" + min + ".." + max + ")";
        else
            return min + ".." + max;
    }

}