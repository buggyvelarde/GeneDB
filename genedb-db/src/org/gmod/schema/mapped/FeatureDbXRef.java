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
@Table(name="feature_dbxref")
public class FeatureDbXRef implements Serializable {

    // Fields
    @SequenceGenerator(name="generator", sequenceName="feature_dbxref_feature_dbxref_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    private int featureDbXRefId;

    @ManyToOne(cascade={},fetch=FetchType.LAZY)

    @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    private DbXRef dbXRef;

    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;

    @Column(name="is_current", unique=false, nullable=false, insertable=true, updatable=true)
    private boolean current = true;

    // Constructors

    /** default constructor */
    FeatureDbXRef() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public FeatureDbXRef(DbXRef dbXRef, Feature feature, boolean current) {
       this.dbXRef = dbXRef;
       this.feature = feature;
       this.current = current;
    }

    // Property accessors
    public int getFeatureDbXRefId() {
        return this.featureDbXRefId;
    }

    public DbXRef getDbXRef() {
        return this.dbXRef;
    }

    void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    public Feature getFeature() {
        return this.feature;
    }

    void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
