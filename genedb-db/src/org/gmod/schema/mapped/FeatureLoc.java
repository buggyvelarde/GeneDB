package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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

    // Fields

    @SequenceGenerator(name = "generator", sequenceName = "featureloc_featureloc_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "featureloc_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureLocId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "srcfeature_id", unique = false, nullable = true, insertable = true, updatable = true)
    private Feature featureBySrcFeatureId;

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature featureByFeatureId;

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

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureLoc")
    private Collection<FeatureLocPub> featureLocPubs;

    // Constructors

    /** default constructor */
    FeatureLoc() {
        // Deliberately empty default constructor
    }

    /** large constructor */
    public FeatureLoc(Feature sourceFeature, Feature feature, Integer fmin,
            boolean fminPartial, Integer fmax, boolean fmaxPartial, Short strand, Integer phase,
            int locGroup, int rank) {
        this.featureBySrcFeatureId = sourceFeature;
        this.featureByFeatureId = feature;
        this.fmin = fmin;
        this.fminPartial = fminPartial;
        this.fmax = fmax;
        this.fmaxPartial = fmaxPartial;
        this.strand = strand;
        this.phase = phase;
        this.locGroup = locGroup;
        this.rank = rank;
    }

    // Property accessors

    public int getFeatureLocId() {
        return this.featureLocId;
    }

    public Feature getSourceFeature() {
        return this.featureBySrcFeatureId;
    }

    void setSourceFeature(Feature featureBySrcFeatureId) {
        this.featureBySrcFeatureId = featureBySrcFeatureId;
    }

    public Feature getFeature() {
        return this.featureByFeatureId;
    }

    void setFeature(Feature featureByFeatureId) {
        this.featureByFeatureId = featureByFeatureId;
    }

    public Integer getFmin() {
        return this.fmin;
    }

    public void setFmin(Integer fmin) {
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
}
