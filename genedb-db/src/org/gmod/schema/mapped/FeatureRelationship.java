package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.propinterface.PropertyI;

import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
@Table(name="feature_relationship")
public class FeatureRelationship implements Serializable,PropertyI {

    // Fields

    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name = "feature_relationship_id", unique = false, nullable = false, insertable = true, updatable = true)
    @SequenceGenerator(name = "generator", sequenceName = "feature_relationship_feature_relationship_id_seq")
    private int featureRelationshipId;

    /*
     * The references subjectFeature and objectFeature are fetched eagerly.
     * The obvious reason is for performance: in almost every case when fetching a
     * relationship, we're interested in the features it relates, so it makes sense
     * to fetch them at the same time.
     */

    @ManyToOne//(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature subjectFeature;

    @ManyToOne//(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "object_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature objectFeature;


    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm type;

    @Column(name = "value", unique = false, nullable = true, insertable = true, updatable = true)
    private String value;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureRelationship")
    private Set<FeatureRelationshipProp> featureRelationshipProps;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureRelationship")
    private Set<FeatureRelationshipPub> featureRelationshipPubs;

    // Constructors

    /** default constructor */
    FeatureRelationship() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public FeatureRelationship(Feature subject, Feature object, CvTerm type, int rank) {
        this.subjectFeature = subject;
        this.objectFeature = object;
        this.type = type;
        this.rank = rank;
    }


    // Property accessors

    public int getFeatureRelationshipId() {
        return this.featureRelationshipId;
    }

    /*
     * The getSubjectFeature and getObjectFeature methods contain explicit
     * checks for Hibernate proxies, because it is very common that the caller
     * needs to check the type of such a feature and downcast, which will not
     * work with a proxy.
     *
     * Under most circumstances we will not have proxies here in any case,
     * because the subject and object features are declared to be eagerly
     * loaded. It can still happen though, if there is already a proxy for
     * the feature present in the Hibernate session.
     */

    public Feature getSubjectFeature() {
        if (subjectFeature instanceof HibernateProxy) {
            HibernateProxy proxyObject = (HibernateProxy) subjectFeature;
            return (Feature) proxyObject.getHibernateLazyInitializer().getImplementation();
        }
        return this.subjectFeature;
    }

    void setSubjectFeature(Feature subjectFeature) {
        this.subjectFeature = subjectFeature;
    }

    public Feature getObjectFeature() {
        if (objectFeature instanceof HibernateProxy) {
            HibernateProxy proxyObject = (HibernateProxy) objectFeature;
            return (Feature) proxyObject.getHibernateLazyInitializer().getImplementation();
        }
        return this.objectFeature;
    }

    public void setObjectFeature(Feature objectFeature) {
        this.objectFeature = objectFeature;
    }

    public CvTerm getType() {
        return this.type;
    }

    void setType(final CvTerm type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public int getRank() {
        return this.rank;
    }

    void setRank(final int rank) {
        this.rank = rank;
    }

    public Collection<FeatureRelationshipProp> getProperties() {
        return Collections.unmodifiableCollection(featureRelationshipProps);
    }

    public Collection<Pub> getPubs() {
        Collection<Pub> pubs = new HashSet<Pub>();
        for (FeatureRelationshipPub featureRelationshipPub: this.featureRelationshipPubs) {
            pubs.add(featureRelationshipPub.getPub());
        }
        return Collections.unmodifiableCollection(pubs);
    }

    public void addPub(Pub pub) {
        this.featureRelationshipPubs.add(new FeatureRelationshipPub(this, pub));
    }
}

