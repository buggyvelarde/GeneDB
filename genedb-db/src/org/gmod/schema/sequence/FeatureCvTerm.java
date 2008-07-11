package org.gmod.schema.sequence;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.utils.Rankable;
import org.gmod.schema.utils.propinterface.PropertyI;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "feature_cvterm")
/*
 * This filter definition depends on the name-mangling algorithm used by
 * Hbernate, but it is more than an order of magnitude faster than the
 * corresponding nested subquery. It also assumes that the feature is
 * being fetched in the same query, of course, which is usually the case
 * anyway in our examples but is forced to be universally true by marking
 * the feature with <code>FetchType.EAGER</code>
 */
@Filter(name="excludeObsoleteFeatures", condition="not feature2_.is_obsolete")
public class FeatureCvTerm implements Serializable, Rankable, PropertyI {

    // Fields

    @SequenceGenerator(name = "generator", sequenceName = "feature_cvterm_feature_cvterm_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name = "feature_cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureCvTermId;

    /*
     * We are rarely if ever interested in a FeatureCvTerm without also being
     * interested in the corresponding Feature and CvTerm, so it makes sense
     * to fetch these eagerly. Besides, the filter definition above relies on
     * the feature being fetched eagerly.
     */
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature feature;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "is_not", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean not;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @OrderBy("rank")
    private List<FeatureCvTermProp> featureCvTermProps = new ArrayList<FeatureCvTermProp>(0);

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Collection<FeatureCvTermPub> featureCvTermPubs = new HashSet<FeatureCvTermPub>(0);

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
     private Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs = new HashSet<FeatureCvTermDbXRef>(0);

    // Constructors

    /** default constructor */
    public FeatureCvTerm() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public FeatureCvTerm(CvTerm cvTerm, Feature feature, Pub pub, boolean not,int rank) {
        this.cvTerm = cvTerm;
        this.feature = feature;
        this.pub = pub;
        this.not = not;
        this.rank = rank;
    }

    // Property accessors
    private int getFeatureCvTermId() {
        return this.featureCvTermId;
    }

    private void setFeatureCvTermId(int featureCvTermId) {
        this.featureCvTermId = featureCvTermId;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }

    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public Feature getFeature() {
        return this.feature;
    }

    private void setFeature(Feature feature) {
        this.feature = feature;
    }

    /**
     * Get the details of the principal publication associated with this FeatureCvTerm, if any.
     * @return a Pub object containing the details of the principal publication associated with
     *  this FeatureCvTerm, or <code>null</code> if there isn't one.
     */
    public Pub getPub() {
        return this.pub;
    }

    private void setPub(Pub pub) {
        this.pub = pub;
    }

    private boolean isNot() {
        return this.not;
    }

    private void setNot(boolean not) {
        this.not = not;
    }

    /**
     * Get the <code>FeatureCvTermProp</code> objects that describe properties of this FeatureCvTerm.
     * @return an unmodifiable collection of <code>FeatureCvTermProp</code> objects
     */
    public Collection<FeatureCvTermProp> getFeatureCvTermProps() {
        return Collections.unmodifiableCollection(this.featureCvTermProps);
    }

    /**
     * Get the <code>FeatureCvTermPub</code> objects that describe publications related to this FeatureCvTerm.
     * @return an unmodifiable collection of <code>FeatureCvTermPub</code> objects
     */
    public Collection<FeatureCvTermPub> getFeatureCvTermPubs() {
        return Collections.unmodifiableCollection(this.featureCvTermPubs);
    }

    private void setFeatureCvTermPubs(Collection<FeatureCvTermPub> featureCvTermPubs) {
        this.featureCvTermPubs = featureCvTermPubs;
    }

    /**
     * Get the <code>FeatureCvTermDbXRef</code> objects that describe the database cross-references
     * for this FeatureCvTerm.
     *
     * @return an unmodifiable collection of <code>FeatureCvTermDbXRef</code> objects
     */
    public Collection<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        return Collections.unmodifiableCollection(this.featureCvTermDbXRefs);
    }

    private void setFeatureCvTermDbXRefs(Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs) {
        this.featureCvTermDbXRefs = featureCvTermDbXRefs;
    }

    public int getRank() {
        return rank;
    }

    private void setRank(int rank) {
        this.rank = rank;
    }

}
