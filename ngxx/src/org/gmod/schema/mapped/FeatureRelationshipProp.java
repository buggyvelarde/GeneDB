package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.utils.propinterface.PropertyI;

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

@Entity
@Table(name = "feature_relationshipprop")
public class FeatureRelationshipProp implements Serializable, PropertyI {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "feature_relationshipprop_feature_relationshipprop_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "feature_relationshipprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureRelationshipPropId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_relationship_id", unique = false, nullable = false, insertable = true, updatable = true)
    private FeatureRelationship featureRelationship;

    @Column(name = "value", unique = false, nullable = true, insertable = true, updatable = true)
    private String value;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "featureRelationshipProp")
    private Collection<FeatureRelationshipPropPub> featureRelationshipPropPubs;


    // Constructors

    FeatureRelationshipProp() {
        // Deliberately empty default constructor
    }

    public FeatureRelationshipProp(CvTerm cvTerm, FeatureRelationship featureRelationship,
            int rank, String value) {
        this.cvTerm = cvTerm;
        this.featureRelationship = featureRelationship;
        this.rank = rank;
        this.value = value;
    }

    // Property accessors

    public int getFeatureRelationshipPropId() {
        return this.featureRelationshipPropId;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public FeatureRelationship getFeatureRelationship() {
        return this.featureRelationship;
    }

    void setFeatureRelationship(FeatureRelationship featureRelationship) {
        this.featureRelationship = featureRelationship;
    }

    public String getValue() {
        return this.value;
    }

    void setValue(String value) {
        this.value = value;
    }

    public int getRank() {
        return this.rank;
    }

    public Collection<Pub> getPubs() {
        Collection<Pub> pubs = new HashSet<Pub>();
        for (FeatureRelationshipPropPub featureRelationshipPropPub : this.featureRelationshipPropPubs) {
            pubs.add(featureRelationshipPropPub.getPub());
        }
        return Collections.unmodifiableCollection(pubs);
    }

    public void addPub(Pub pub) {
        this.featureRelationshipPropPubs.add(new FeatureRelationshipPropPub(this, pub));
    }
}
