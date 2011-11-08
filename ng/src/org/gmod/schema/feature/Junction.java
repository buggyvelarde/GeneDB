package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;


import org.gmod.schema.mapped.CvTerm;

import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.Organism;

import java.sql.Timestamp;
import javax.persistence.Entity;

import javax.persistence.Transient;

/**
 * Describes a feature with an extent of zero.
 * 
 * @author gv1
 *
 */
@FeatureType(cv="sequence", term="junction")
@Entity
public class Junction extends Feature implements Comparable<Junction>{
    
    public Junction() {
        // empty
    }
    
    public Junction(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned, dateAccessioned);
    }

    public Junction(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp timeAccessioned, Timestamp timeLastModified) {
        super(organism, uniqueName, analysis, obsolete, timeAccessioned, timeLastModified);
    }
    
    @Transient
    private int fmin;
    
    @Transient
    private int fmax;
    
    @Transient
    private int srcFeatureId;
    
    @Transient
    private short strand;
    
    @Transient
    private boolean locLoaded;
    
    private void loadLoc() {
        if (locLoaded) {
            return;
        }
        FeatureLoc featureLoc = getRankZeroFeatureLoc();
        
        fmin = featureLoc.getFmin();
        fmax = featureLoc.getFmax();
        
        // TODO ideally a Junction should have a length of zero, but this breaks writedb_entry
        // must discuss 
        // if ((fmax - fmin ) != 0)
        //    throw new IllegalStateException("A junction's fmin must be equal to its fmax.");
        
        if (featureLoc.getStrand() != null) {
            strand = featureLoc.getStrand();
        } else {
            strand = 0;
        }
        
        Feature feature = featureLoc.getSourceFeature();
        if (feature != null) {
            srcFeatureId = feature.getFeatureId();
        }
    }
    
    @Override
    public int compareTo(Junction other) {
        this.loadLoc();
        other.loadLoc();

        if (this.srcFeatureId != other.srcFeatureId) {
            return this.srcFeatureId - other.srcFeatureId;
        }
        if (this.fmin != other.fmin) {
            return this.fmin - other.fmin;
        }
        if (this.fmax != other.fmax) {
            return other.fmax - this.fmax;
        }
        return this.getFeatureId() - other.getFeatureId();
    }

    public int getFmin() {
        loadLoc();
        return fmin;
    }

    public int getFmax() {
        loadLoc();
        return fmax;
    }
    
}
