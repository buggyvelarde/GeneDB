package org.gmod.schema.cv;



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
@Table(name="cvterm_dbxref")
public class CvTermDbXRef implements Serializable {

    // Fields    
    @Id
    @Column(name="cvterm_dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int cvTermDbXRefId;
    
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
     private DbXRef dbXRef;
     
    @Column(name="is_for_definition", unique=false, nullable=false, insertable=true, updatable=true)
     private int isForDefinition;

     // Constructors

    /** default constructor */
    public CvTermDbXRef() {
    }

    /** full constructor */
    private CvTermDbXRef(CvTerm cvTerm, DbXRef dbXRef, int isForDefinition) {
       this.cvTerm = cvTerm;
       this.dbXRef = dbXRef;
       this.isForDefinition = isForDefinition;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#getCvTermDbXRefId()
     */
    private int getCvTermDbXRefId() {
        return this.cvTermDbXRefId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#setCvTermDbXRefId(int)
     */
    private void setCvTermDbXRefId(int cvTermDbXRefId) {
        this.cvTermDbXRefId = cvTermDbXRefId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#getCvTerm()
     */
    private CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#setCvTerm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#getDbXRef()
     */
    private DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#setDbXRef(org.gmod.schema.general.DbXRefI)
     */
    private void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#getIsForDefinition()
     */
    private int getIsForDefinition() {
        return this.isForDefinition;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermDbXRefI#setIsForDefinition(int)
     */
    private void setIsForDefinition(int isForDefinition) {
        this.isForDefinition = isForDefinition;
    }




}


