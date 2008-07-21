package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Transient;

@FeatureType(cv="sequence", term="region")
@Entity
public class Region extends Feature implements Comparable<Region> {

    public Region() {}
    public Region(Organism organism, CvTerm cvTerm, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, cvTerm, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }

    @Transient
    private boolean locLoaded = false;
    @Transient
    private int fmin;
    @Transient
    private int fmax;
    @Transient
    private short strand;
    @Transient
    private int srcFeatureId;

    private void loadLoc() {
        if (locLoaded) {
            return;
        }
        FeatureLoc featureLoc = getRankZeroFeatureLoc();
        fmin = featureLoc.getFmin();
        fmax = featureLoc.getFmax();
        strand = featureLoc.getStrand();
        srcFeatureId = featureLoc.getSourceFeature().getFeatureId();
    }

    public int compareTo(Region other) {
        this.loadLoc();
        other.loadLoc();

        if (this.srcFeatureId != other.srcFeatureId) {
            return this.srcFeatureId - other.srcFeatureId;
        }
        if (this.strand != other.strand) {
            return this.strand - other.strand;
        }
        if (this.fmin != other.fmin) {
            return this.fmin - other.fmin;
        }
        if (this.fmax != other.fmax) {
            return other.fmax - this.fmax;
        }
        return this.getFeatureId() - other.getFeatureId();
    }
    /**
     * Get the exon location as a string, in interbase coordinates.
     *
     * @return
     */
    @Transient
    protected String getLocAsString() {
        loadLoc();
        if (strand == -1) {
            return "(" + fmin + ".." + fmax + ")";
        } else {
            return fmin + ".." + fmax;
        }
    }
    /**
     * Get the component location as a string, in traditional coordinates (i.e. inclusive base coordinates with the origin at 1).
     *
     * @return
     */
    @Transient
    protected String getTraditionalLocAsString() {
        loadLoc();
        if (strand == -1) {
            return "(" + (fmin+1) + "-" + fmax + ")";
        } else {
            return (fmin+1) + "-" + fmax;
        }
    }
}
