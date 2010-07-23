package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import org.genedb.db.dao.CvDao;

import org.gmod.schema.utils.Rankable;
import org.gmod.schema.utils.propinterface.PropertyI;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
import javax.persistence.Transient;

@Configurable
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
public class FeatureCvTerm implements Serializable, Rankable, PropertyI, HasPubsAndDbXRefs {

    private static final Logger logger = Logger.getLogger(FeatureCvTerm.class);

    // Fields

    @Autowired
    private transient CvDao cvDao;

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

    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @Column(name = "is_not", unique = false, nullable = false, insertable = true, updatable = true)
    private boolean not;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @OrderBy("rank")
    private List<FeatureCvTermProp> featureCvTermProps = new ArrayList<FeatureCvTermProp>();

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Collection<FeatureCvTermPub> featureCvTermPubs = new HashSet<FeatureCvTermPub>();

    @OneToMany(cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "featureCvTerm")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Collection<FeatureCvTermDbXRef> featureCvTermDbXRefs = new HashSet<FeatureCvTermDbXRef>();

    // Constructors

    /** default constructor */
    FeatureCvTerm() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public FeatureCvTerm(CvTerm cvTerm, Feature feature, Pub pub, boolean not, int rank) {
        this.cvTerm = cvTerm;
        this.feature = feature;
        this.pub = pub;
        this.not = not;
        this.rank = rank;
    }

    // Property accessors
    public int getFeatureCvTermId() {
        return this.featureCvTermId;
    }

    /**
     * An alias for {@link #getCvTerm}, which exists
     * so that the generic interface <code>PropertyI</code>
     * can be used.
     */
    public CvTerm getType() {
        return getCvTerm();
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

    void setFeature(Feature feature) {
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

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public boolean isNot() {
        return this.not;
    }

    /**
     * Get the <code>FeatureCvTermProp</code> objects that describe properties of this FeatureCvTerm.
     * @return an unmodifiable collection of <code>FeatureCvTermProp</code> objects
     */
    public List<FeatureCvTermProp> getFeatureCvTermProps() {
        return Collections.unmodifiableList(this.featureCvTermProps);
    }

    public FeatureCvTermProp addProp(String cvName, String cvTermName, String value) {
        CvTerm propType = cvDao.getCvTermByNameAndCvName(cvTermName, cvName);
        if (propType == null) {
            throw new RuntimeException(String.format("Could not find CV term '%s' in CV '%s'",
                cvTermName, cvName));
        }
        return addProp(propType, value);
    }
    public FeatureCvTermProp addProp(CvTerm type, String value) {
        FeatureCvTermProp featureCvTermProp = new FeatureCvTermProp(type, this, value, 0);
        this.featureCvTermProps.add(featureCvTermProp);
        return featureCvTermProp;
    }

    public FeatureCvTermProp addPropIfNotNull(String cvName, String cvTermName, String value) {
        if (value != null) {
            return addProp(cvName, cvTermName, value);
        } else {
            return null;
        }
    }

    @Transient
    private Object featureCvTermPubsLock = new Object();

    /**
     * Get the <code>FeatureCvTermPub</code> objects that describe publications related to this FeatureCvTerm.
     * It is usually easier to use the method {@link #getPubs()} instead.
     *
     * @return an unmodifiable collection of <code>FeatureCvTermPub</code> objects
     */
    public Collection<FeatureCvTermPub> getFeatureCvTermPubs() {
        synchronized(featureCvTermPubsLock) {
            return Collections.unmodifiableCollection(this.featureCvTermPubs);
        }
    }

    public void addFeatureCvTermPub(FeatureCvTermPub featureCvTermPub) {
        logger.trace(String.format("Adding FeatureCvTermPub (%s) to FeatureCvTerm",
            featureCvTermPub, this));

        synchronized(featureCvTermPubsLock) {
            featureCvTermPub.setFeatureCvTerm(this);
            this.featureCvTermPubs.add(featureCvTermPub);
        }
    }

    public FeatureCvTermPub addPub(Pub pub) {
        FeatureCvTermPub featureCvTermPub = new FeatureCvTermPub(this, pub);
        addFeatureCvTermPub(featureCvTermPub);
        return featureCvTermPub;
    }

    /**
     * Get all the publications associated with this FeatureCvTerm.
     *
     * @return an unmodifiable collection of <code>Pub</code> objects
     */
    @Transient
    public Collection<Pub> getPubs() {
        Collection<Pub> pubs = new HashSet<Pub>();
        for (FeatureCvTermPub featureCvTermPub: this.featureCvTermPubs) {
            pubs.add(featureCvTermPub.getPub());
        }
        return Collections.unmodifiableCollection(pubs);
    }

    @Transient
    private Object featureCvTermDbXRefsLock = new Object();

    /**
     * Get the <code>FeatureCvTermDbXRef</code> objects that describe the database cross-references
     * for this FeatureCvTerm.
     *
     * @return an unmodifiable collection of <code>FeatureCvTermDbXRef</code> objects
     */
    public Collection<FeatureCvTermDbXRef> getFeatureCvTermDbXRefs() {
        synchronized(featureCvTermDbXRefsLock) {
            return Collections.unmodifiableCollection(this.featureCvTermDbXRefs);
        }
    }

    public void addFeatureCvTermDbXRef(FeatureCvTermDbXRef featureCvTermDbXRef) {
        synchronized(featureCvTermDbXRefsLock) {
            this.featureCvTermDbXRefs.add(featureCvTermDbXRef);
            featureCvTermDbXRef.setFeatureCvTerm(this);
        }
    }

    public FeatureCvTermDbXRef addDbXRef(DbXRef dbXRef) {
        FeatureCvTermDbXRef featureCvTermDbXRef = new FeatureCvTermDbXRef(this, dbXRef);
        addFeatureCvTermDbXRef(featureCvTermDbXRef);
        return featureCvTermDbXRef;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return String.format("FeatureCvTerm(ID=%d, feature=%s, cvTerm=%s, rank=%d, not=%b)",
            getFeatureCvTermId(), getFeature(), getCvTerm(), getRank(), isNot());
    }

}
