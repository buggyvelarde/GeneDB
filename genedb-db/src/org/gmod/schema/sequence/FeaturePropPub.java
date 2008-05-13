package org.gmod.schema.sequence;

import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.pub.Pub;

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
@Table(name = "featureprop_pub")
public class FeaturePropPub implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "featureprop_pub_featureprop_pub_id_seq")
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "featureprop_pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int featurePropPubId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "featureprop_id", unique = false, nullable = false, insertable = true, updatable = true)
    private FeatureProp featureProp;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Pub pub;

    // Property accessors

    public int getFeaturePropPubId() {
        return this.featurePropPubId;
    }

    public void setFeaturePropPubId(int featurePropPubId) {
        this.featurePropPubId = featurePropPubId;
    }

    public FeatureProp getFeatureProp() {
        return this.featureProp;
    }

    public void setFeatureProp(FeatureProp featureProp) {
        this.featureProp = featureProp;
    }

    public Pub getPub() {
        return this.pub;
    }

    public void setPub(Pub pub) {
        this.pub = pub;
    }
}
