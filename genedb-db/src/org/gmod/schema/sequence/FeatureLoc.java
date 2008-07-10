package org.gmod.schema.sequence;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Collection;

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
    private FeatureLoc() {
        // Deliberately empty default constructor
    }

    /** large constructor */
    public FeatureLoc(Feature featureBySrcfeatureId, Feature featureByFeatureId, Integer fmin,
            boolean fminPartial, Integer fmax, boolean fmaxPartial, Short strand, Integer phase,
            int locGroup, int rank) {
        this.featureBySrcFeatureId = featureBySrcfeatureId;
        this.featureByFeatureId = featureByFeatureId;
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

    public Feature getFeatureBySrcFeatureId() {
        return this.featureBySrcFeatureId;
    }

    private void setFeatureBySrcFeatureId(Feature featureBySrcFeatureId) {
        this.featureBySrcFeatureId = featureBySrcFeatureId;
    }

    public Feature getFeatureByFeatureId() {
        return this.featureByFeatureId;
    }

    void setFeatureByFeatureId(Feature featureByFeatureId) {
        this.featureByFeatureId = featureByFeatureId;
    }

    public Integer getFmin() {
        return this.fmin;
    }

    public void setFmin(Integer fmin) {
        this.fmin = fmin;
    }

    private boolean isFminPartial() {
        return this.fminPartial;
    }

    private void setFminPartial(boolean fminPartial) {
        this.fminPartial = fminPartial;
    }

    public Integer getFmax() {
        return this.fmax;
    }

    public void setFmax(Integer fmax) {
        this.fmax = fmax;
    }

    private boolean isFmaxPartial() {
        return this.fmaxPartial;
    }

    private void setFmaxPartial(boolean fmaxPartial) {
        this.fmaxPartial = fmaxPartial;
    }

    public Short getStrand() {
        return this.strand;
    }

    private void setStrand(Short strand) {
        this.strand = strand;
    }

    private Integer getPhase() {
        return this.phase;
    }

    private void setPhase(Integer phase) {
        this.phase = phase;
    }

    private String getResidueInfo() {
        return this.residueInfo;
    }

    private void setResidueInfo(String residueInfo) {
        this.residueInfo = residueInfo;
    }

    private int getLocGroup() {
        return this.locGroup;
    }

    private void setLocGroup(int locGroup) {
        this.locGroup = locGroup;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    private Collection<FeatureLocPub> getFeatureLocPubs() {
        return this.featureLocPubs;
    }

    private void setFeaturelocPubs(Collection<FeatureLocPub> featureLocPubs) {
        this.featureLocPubs = featureLocPubs;
    }

    private int getFeatureLocId() {
        return this.featureLocId;
    }

    private void setFeatureLocId(int featureLocId) {
        this.featureLocId = featureLocId;
    }

}
