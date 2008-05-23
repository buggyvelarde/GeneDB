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

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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

    /**
     * This field does not participate in the Hibernate mapping, and is neither
     * automatically populated nor persisted. It may be explicitly assigned
     * using setSrcFeatureId(int). It's currently used by Artemis to cache the
     * equivalent of getFeatureBySrcFeatureId().getFeatureId().
     */
    @Transient
    private int srcFeatureId;

    // Constructors

    /** default constructor */
    public FeatureLoc() {
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

    public void setFeatureBySrcFeatureId(Feature featureBySrcFeatureId) {
        this.featureBySrcFeatureId = featureBySrcFeatureId;
    }

    public Feature getFeatureByFeatureId() {
        return this.featureByFeatureId;
    }

    public void setFeatureByFeatureId(Feature featureByFeatureId) {
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

    public Collection<FeatureLocPub> getFeatureLocPubs() {
        return this.featureLocPubs;
    }

    public void setFeaturelocPubs(Collection<FeatureLocPub> featureLocPubs) {
        this.featureLocPubs = featureLocPubs;
    }

    public int getFeatureLocId() {
        return this.featureLocId;
    }

    public void setFeatureLocId(int featureLocId) {
        this.featureLocId = featureLocId;
    }

    /**
     * Return the (unmapped) srcFeatureId property. This must be explicitly
     * populated beforehand using setSrcFeatureId(int); it is not automatically
     * populated by Hibernate.
     * 
     * Perhaps you really wanted getFeatureBySrcFeatureId().getFeatureId()?
     * 
     * @return The value of the srcFeatureId property
     */
    public int getSrcFeatureId() {
        return srcFeatureId;
    }

    /**
     * Set the value of the (unmapped) srcFeatureId property. This is not
     * automatically populated by Hibernate, and must be set explicitly if it is
     * used. Currently used only by Artemis.
     * 
     * @param srcFeatureId
     */
    public void setSrcFeatureId(int srcFeatureId) {
        this.srcFeatureId = srcFeatureId;
    }
}
