package org.gmod.schema.sequence.feature;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;

@Entity
public abstract class TranscriptComponent extends Feature  implements Comparable<TranscriptComponent> {

    @Transient private boolean locLoaded = false;
    @Transient private int fmin;
    @Transient private int fmax;
    @Transient private short strand;

    private void loadLoc() {
        if (locLoaded)
            return;
        FeatureLoc featureLoc = getRankZeroFeatureLoc();
        fmin = featureLoc.getFmin();
        fmax = featureLoc.getFmax();
        strand = featureLoc.getStrand();
    }

    /**
     * Get the exon location as a string, in interbase coordinates.
     *
     * @return
     */
    @Transient
    protected String getLocAsString() {
        loadLoc();
        if (strand == -1)
            return "(" + fmin + ".." + fmax + ")";
        else
            return fmin + ".." + fmax;
    }

    /**
     * Get the exon location as a string, in traditional coordinates (i.e. inclusive base coordinates with the origin at 1).
     *
     * @return
     */
    @Transient
    protected String getTraditionalLocAsString() {
        loadLoc();
        if (strand == -1)
            return "(" + (fmin+1) + "-" + fmax + ")";
        else
            return (fmin+1) + "-" + fmax;
    }

    public int compareTo(TranscriptComponent other) {
        this.loadLoc();
        other.loadLoc();

        if (this.strand != other.strand)
            return this.strand - other.strand;
        if (this.fmin != other.fmin)
            return this.fmin - other.fmin;
        if (this.fmax != other.fmax)
            return this.fmax - other.fmax;

        return 0;
    }
}
