package org.gmod.schema.mapped;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.CascadeType;
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
@Table(name = "feature_pub")
public class FeaturePub implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "feature_pub_feature_pub_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "feature_pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featurePubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Feature feature;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    // Constructors

    FeaturePub() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public FeaturePub(Feature feature, Pub pub) {
        this.feature = feature;
        this.pub = pub;
    }

    // Property accessors

    public int getFeaturePubId() {
        return this.featurePubId;
    }

    public Feature getFeature() {
        return this.feature;
    }

    void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }
}
