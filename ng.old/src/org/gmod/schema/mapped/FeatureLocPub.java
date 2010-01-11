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
@Table(name = "featureloc_pub")
public class FeatureLocPub implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "featureloc_pub_featureloc_pub_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "featureloc_pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featureLocPubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "featureloc_id", unique = false, nullable = false, insertable = true, updatable = true)
    private FeatureLoc featureLoc;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;


    // Constructors

    FeatureLocPub() {
        // Deliberately empty default constructor
    }

    public FeatureLocPub(FeatureLoc featureLoc, Pub pub) {
        this.featureLoc = featureLoc;
        this.pub = pub;
    }

    // Property accessors
    public int getFeatureLocPubId() {
        return this.featureLocPubId;
    }

    public FeatureLoc getFeatureLoc() {
        return this.featureLoc;
    }

    void setFeatureLoc(FeatureLoc featureloc) {
        this.featureLoc = featureloc;
    }

    public Pub getPub() {
        return this.pub;
    }
}
