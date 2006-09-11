package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.general.DbXRef;

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
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        
        @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    private Feature feature;
     
    @Column(name="is_current", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean current;

     // Constructors

    /** default constructor */
    public FeatureDbXRef() {
    }

    /** full constructor */
    public FeatureDbXRef(DbXRef dbXRef, Feature feature, boolean current) {
       this.dbXRef = dbXRef;
       this.feature = feature;
       this.current = current;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#getFeatureDbXRefId()
     */
    private int getFeatureDbXRefId() {
        return this.featureDbXRefId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#setFeatureDbXRefId(int)
     */
    private void setFeatureDbXRefId(int featureDbXRefId) {
        this.featureDbXRefId = featureDbXRefId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#getDbxref()
     */
    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#setDbxref(org.gmod.schema.general.DbXRefI)
     */
    private void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#getFeature()
     */
    private Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#setFeature(org.genedb.db.jpa.Feature)
     */
    private void setFeature(Feature feature) {
        this.feature = feature;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#isCurrent()
     */
    public boolean isCurrent() {
        return this.current;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureDbXRefI#setCurrent(boolean)
     */
    private void setCurrent(boolean current) {
        this.current = current;
    }




}


