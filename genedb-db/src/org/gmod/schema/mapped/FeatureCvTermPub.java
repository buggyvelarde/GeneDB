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
@Table(name="feature_cvterm_pub")
public class FeatureCvTermPub implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="feature_cvterm_pub_feature_cvterm_pub_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_cvterm_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermPubId;

     @ManyToOne(cascade={},fetch=FetchType.LAZY)
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;

     @ManyToOne(cascade={},fetch=FetchType.LAZY)
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;

     // Constructors

    /** default constructor */
    public FeatureCvTermPub() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public FeatureCvTermPub(FeatureCvTerm featureCvTerm, Pub pub) {
       this.pub = pub;
       this.featureCvTerm = featureCvTerm;
    }

    // Property accessors
    public int getFeatureCvTermPubId() {
        return this.featureCvTermPubId;
    }

    public Pub getPub() {
        return this.pub;
    }

    void setPub(Pub pub) {
        this.pub = pub;
    }

    public FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }

    void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }
}


