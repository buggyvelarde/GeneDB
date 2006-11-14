package org.gmod.schema.sequence;

import org.gmod.schema.general.DbXRef;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="feature_cvterm_dbxref")
public class FeatureCvTermDbXRef implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_cvterm_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermDbXRefId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;

     // Constructors

    /** default constructor */
    public FeatureCvTermDbXRef() {
    }

    /** full constructor */
    public FeatureCvTermDbXRef(DbXRef dbXRef, FeatureCvTerm featureCvTerm) {
       this.dbXRef = dbXRef;
       this.featureCvTerm = featureCvTerm;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#getFeatureCvTermDbXrefId()
     */
    private int getFeatureCvTermDbXRefId() {
        return this.featureCvTermDbXRefId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#setFeatureCvTermDbXrefId(int)
     */
    private void setFeatureCvTermDbXRefId(int featureCvTermDbXRefId) {
        this.featureCvTermDbXRefId = featureCvTermDbXRefId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#getDbxref()
     */
    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#setDbxref(org.gmod.schema.general.DbXRefI)
     */
    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#getFeatureCvterm()
     */
    public FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermDbXRefI#setFeatureCvterm(org.gmod.schema.sequence.FeatureCvTermI)
     */
    public void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }




}


