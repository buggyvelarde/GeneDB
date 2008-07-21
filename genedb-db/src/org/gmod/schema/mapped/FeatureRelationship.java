package org.gmod.schema.mapped;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.propinterface.PropertyI;

import org.hibernate.annotations.Filter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

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

@Entity
@Table(name="feature_relationship")
@Filter(name="excludeObsoleteFeatures", condition="2 = (select count(*) from feature where feature.feature_id in (subject_id, object_id) and not feature.is_obsolete)")
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
     *
     * However, there are also two more subtle reasons:<ol>
     * <li> If they were lazily-fetched, the associated objects would
     *      necessarily be objects of a Hibernate wrapper class, so it
     *      would be impossible to do tests such as
     *      <code>rel.getFeatureByObjectId() instanceof ProductiveTranscript</code>
     *      and the like, because the wrapper objects can't respect the
     *      hierarchy of feature classes.
     * <li> The condition used, when the excludeObsoleteFeatures filter is
     *      active, to restrict Feature.featureRelationshipsForSubjectId and
     *      Feature.featureRelationshipsForObjectId, relies on the fact that
     *      the corresponding query also retrieves the associated features.
     *      It does so only because of the eager fetching here. If these
     *      properties were to be lazily fetched, that condition would need
     *      to be replaced by a less efficient nested subquery (which might
     *      well cancel out any perceived advantages of lazy fetching here).
     * </ol>
     */

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature subjectFeature;

    @ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
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
    private Collection<FeatureRelationshipProp> featureRelationshipProps;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureRelationship")
    private Collection<FeatureRelationshipPub> featureRelationshipPubs;

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

    public Feature getSubjectFeature() {
        return this.subjectFeature;
    }

    void setSubjectFeature(Feature featureBySubjectId) {
        this.subjectFeature = featureBySubjectId;
    }

    public Feature getObjectFeature() {
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


