package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.StrandedLocation;

import org.apache.log4j.Logger;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "featureloc")
public class FeatureLoc implements Serializable {

    private static final Logger logger = Logger.getLogger(FeatureLoc.class);

    // Fields

    @SequenceGenerator(name = "generator", sequenceName = "featureloc_featureloc_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "featureloc_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureLocId;

    @ManyToOne(cascade = {})
    @LazyToOne(value=LazyToOneOption.NO_PROXY)
    @JoinColumn(name = "srcfeature_id", unique = false, nullable = true, insertable = true, updatable = true)
    private Feature sourceFeature;

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature feature;

    @Column(name = "fmin", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer fmin;

    @Column(name = "is_fmin_partial", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean fminPartial;

    @Column(name = "fmax", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer fmax;

    @Column(name = "is_fmax_partial", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean fmaxPartial;

    @Column(name = "strand", unique = false, nullable = true, insertable = true, updatable = true)
    private Short strand;

    @Column(name = "phase", unique = false, nullable = true, insertable = true, updatable = true)
    private Integer phase;

    @Column(name = "residue_info", unique = false, nullable = true, insertable = true, updatable = true)
    private String residueInfo;

    @Column(name = "locgroup", unique = false, nullable = false, insertable = true, updatable = true)
    private int locGroup;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureLoc")
    private Set<FeatureLocPub> featureLocPubs;

    // Constructors

    /** default constructor */
    FeatureLoc() {
        // Deliberately empty default constructor
    }


    /** large constructor */
    public FeatureLoc(Feature sourceFeature, Feature feature, Integer fmin,
            boolean fminPartial, Integer fmax, boolean fmaxPartial, Short strand, Integer phase,
            int locGroup, int rank) {
        this.sourceFeature = sourceFeature;
        this.feature = feature;
        this.fmin = fmin;
        this.fminPartial = fminPartial;
        this.fmax = fmax;
        this.fmaxPartial = fmaxPartial;
        this.strand = strand;
        this.phase = phase;
        this.locGroup = locGroup;
        this.rank = rank;
    }

    public FeatureLoc(Feature sourceFeature, Feature feature, int fmin, int fmax, int strand, Integer phase) {
        this(sourceFeature, feature, fmin, false, fmax, false, (short) strand, phase, 0, 0);
    }

    public FeatureLoc(Feature sourceFeature, Feature feature, int fmin, int fmax, int strand, Integer phase, int rank) {
        this(sourceFeature, feature, fmin, false, fmax, false, (short) strand, phase, 0, rank);
    }

    public FeatureLoc(Feature sourceFeature, Feature feature, int fmin, int fmax, int strand, Integer phase, int locgroup, int rank) {
        this(sourceFeature, feature, fmin, false, fmax, false, (short) strand, phase, locgroup, rank);
    }

    FeatureLoc(Feature parent, Feature child, StrandedLocation location) {
        StrandedLocation loc = location.getInterbaseVersion();
        this.sourceFeature = parent;
        this.feature = child;
        this.fmin = loc.getMin();
        this.fminPartial = loc.isMinPartial();
        this.fmax = loc.getMax();
        this.fmaxPartial = loc.isMaxPartial();
        this.strand = loc.getStrand().getValue();
        this.phase = 0;
        this.locGroup = 0;
        this.rank = 0;
    }


    // Property accessors

    public int getFeatureLocId() {
        return this.featureLocId;
    }

    public Feature getSourceFeature() {
        return this.sourceFeature;
    }

    public void setSourceFeature(Feature sourceFeature) {
        this.sourceFeature = sourceFeature;
    }

    public Feature getFeature() {
        return this.feature;
    }

    void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Integer getFmin() {
        return this.fmin;
    }

    public void setFmin(Integer fmin) {
        logger.trace(String.format("Feature '%s' located on '%s': setting fmin to %d",
            getFeature().getUniqueName(), getSourceFeature().getUniqueName(), fmin));
        this.fmin = fmin;
    }

    public boolean isFminPartial() {
        return this.fminPartial;
    }

    public void setFminPartial(boolean fminPartial) {
        this.fminPartial = fminPartial;
    }

    public Integer getFmax() {
        return this.fmax;
    }

    public void setFmax(Integer fmax) {
        logger.trace(String.format("Feature '%s' located on '%s': setting fmax to %d",
            getFeature().getUniqueName(), getSourceFeature().getUniqueName(), fmax));
        this.fmax = fmax;
    }

    public boolean isFmaxPartial() {
        return this.fmaxPartial;
    }

    public void setFmaxPartial(boolean fmaxPartial) {
        this.fmaxPartial = fmaxPartial;
    }

    public Short getStrand() {
        return this.strand;
    }

    public void setStrand(Short strand) {
        this.strand = strand;
    }

    public Integer getPhase() {
        return this.phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public String getResidueInfo() {
        return this.residueInfo;
    }

    public void setResidueInfo(String residueInfo) {
        this.residueInfo = residueInfo;
    }

    public int getLocGroup() {
        return this.locGroup;
    }

    public void setLocGroup(int locGroup) {
        this.locGroup = locGroup;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Transient
    public int getLength() {
        return fmax - fmin;
    }

    /**
     * Get all the FeatureLocPub relations associated with this
     * object. Usually the {@link #getPubs()} method is more
     * useful.
     *
     * @return an unmodifiable collection of <code>FeatureLocPub</code> objects
     */
    public Collection<FeatureLocPub> getFeatureLocPubs() {
        return Collections.unmodifiableCollection(this.featureLocPubs);
    }

    /**
     * Get the Pubs associated with this FeatureLoc.
     * @return an unmodifiable collection of <code>Pub</code> objects.
     */
    @Transient
    public Collection<Pub> getPubs() {
        Collection<Pub> pubs = new HashSet<Pub>();
        for (FeatureLocPub featureLocPub: this.featureLocPubs) {
            pubs.add(featureLocPub.getPub());
        }
        return Collections.unmodifiableCollection(pubs);
    }

    public void addPub(Pub pub) {
        this.featureLocPubs.add(new FeatureLocPub(this, pub));
    }

    /*
     * The equals and hashCode methods were generated by Eclipse using
     * the fields fmin, fmax, strand, feature, and sourceFeature.
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        result = prime * result + ((fmax == null) ? 0 : fmax.hashCode());
        result = prime * result + ((fmin == null) ? 0 : fmin.hashCode());
        result = prime * result + ((sourceFeature == null) ? 0 : sourceFeature.hashCode());
        result = prime * result + ((strand == null) ? 0 : strand.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FeatureLoc other = (FeatureLoc) obj;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        if (fmax == null) {
            if (other.fmax != null)
                return false;
        } else if (!fmax.equals(other.fmax))
            return false;
        if (fmin == null) {
            if (other.fmin != null)
                return false;
        } else if (!fmin.equals(other.fmin))
            return false;
        if (sourceFeature == null) {
            if (other.sourceFeature != null)
                return false;
        } else if (!sourceFeature.equals(other.sourceFeature))
            return false;
        if (strand == null) {
            if (other.strand != null)
                return false;
        } else if (!strand.equals(other.strand))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("{fmin=%d, fmax=%d, strand=%d, sourceFeatureId=%d}",
            fmin, fmax, strand, sourceFeature == null ? -1 : sourceFeature.getFeatureId());
    }

}
