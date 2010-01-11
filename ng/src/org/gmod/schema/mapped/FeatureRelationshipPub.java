package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "feature_relationship_pub")
public class FeatureRelationshipPub implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "feature_relationship_pub_feature_relationship_pub_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "feature_relationship_pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureRelationshipPubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_relationship_id", unique = false, nullable = false, insertable = true, updatable = true)
    private FeatureRelationship featureRelationship;

    // Constructors

    FeatureRelationshipPub() {
        // Deliberately empty default constructor
    }

    public FeatureRelationshipPub(FeatureRelationship featureRelationship, Pub pub) {
        this.featureRelationship = featureRelationship;
        this.pub = pub;
    }

    // Property accessors

    public int getFeatureRelationshipPubId() {
        return this.featureRelationshipPubId;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public FeatureRelationship getFeatureRelationship() {
        return this.featureRelationship;
    }

    void setFeatureRelationship(FeatureRelationship featureRelationship) {
        this.featureRelationship = featureRelationship;
    }
}
