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
@Table(name = "feature_relationshipprop_pub")
public class FeatureRelationshipPropPub implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "feature_relationshipprop_pub_feature_relationshipprop_pub_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "feature_relationshipprop_pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureRelationshipPropPubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_relationshipprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private FeatureRelationshipProp featureRelationshipProp;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;


    // Constructors

    FeatureRelationshipPropPub() {
        // Deliberately empty default constructor
    }

    public FeatureRelationshipPropPub(FeatureRelationshipProp featureRelationshipProp, Pub pub) {
        this.featureRelationshipProp = featureRelationshipProp;
        this.pub = pub;
    }


    // Property accessors

    public int getFeatureRelationshipPropPubId() {
        return this.featureRelationshipPropPubId;
    }

    public FeatureRelationshipProp getFeatureRelationshipProp() {
        return this.featureRelationshipProp;
    }

    void setFeatureRelationshipProp(FeatureRelationshipProp featureRelationshipProp) {
        this.featureRelationshipProp = featureRelationshipProp;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }
}
