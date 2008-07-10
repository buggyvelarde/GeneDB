package org.gmod.schema.sequence;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.utils.Rankable;
import org.gmod.schema.utils.propinterface.PropertyI;
import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.util.Collection;
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

@Entity
@Table(name = "feature_cvterm")
@Filter(name="excludeObsoleteFeatures", condition="exists (select 8 from feature where feature.feature_id = feature_id and not feature.is_obsolete)")
public class FeatureCvTerm implements Serializable, Rankable, PropertyI {

    // Fields

    @SequenceGenerator(name = "generator", sequenceName = "feature_cvterm_feature_cvterm_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name = "feature_cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureCvTermId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "cvterm_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature feature;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "is_not", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean not;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    private Collection<FeatureCvTermProp> featureCvTermProps = new HashSet<FeatureCvTermProp>(0);

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    private Collection<FeatureCvTermPub> featureCvTermPubs = new HashSet<FeatureCvTermPub>(0);

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
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

    private Pub getPub() {
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

    private Collection<FeatureCvTermProp> getFeatureCvTermProps() {
        return this.featureCvTermProps;
    }

    private void setFeatureCvTermProps(Collection<FeatureCvTermProp> featureCvTermProps) {
        this.featureCvTermProps = featureCvTermProps;
    }

    private Collection<FeatureCvTermPub> getFeatureCvTermPubs() {
        return this.featureCvTermPubs;
    }

    private void setFeatureCvTermPubs(Collection<FeatureCvTermPub> featureCvTermPubs) {
        this.featureCvTermPubs = featureCvTermPubs;
    }

    private Collection<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        return this.featureCvTermDbXRefs;
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
