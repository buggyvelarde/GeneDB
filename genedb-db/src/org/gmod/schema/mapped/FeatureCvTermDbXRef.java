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
@Table(name="feature_cvterm_dbxref")
public class FeatureCvTermDbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="feature_cvterm_dbxref_feature_cvterm_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_cvterm_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermDbXRefId;

     @ManyToOne(cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
         @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;

     @ManyToOne(cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER)
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;

     // Constructors

    /** default constructor */
    FeatureCvTermDbXRef() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public FeatureCvTermDbXRef(FeatureCvTerm featureCvTerm, DbXRef dbXRef) {
       this.featureCvTerm = featureCvTerm;
       this.dbXRef = dbXRef;
    }


    // Property accessors
    public int getFeatureCvTermDbXRefId() {
        return this.featureCvTermDbXRefId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }

    void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }
}

