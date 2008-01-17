package org.gmod.schema.cv;



import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="cvtermprop")
public class CvTermProp implements Serializable {

    // Fields    
     @Id
    @Column(name="cvtermprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int cvTermPropId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         @JoinColumn(name="cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTermByCvTermId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTermByTypeId;
     
     @Column(name="value", unique=false, nullable=false, insertable=true, updatable=true)
     private String value;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#getCvTermpropId()
     */
    private int getCvTermPropId() {
        return this.cvTermPropId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#setCvTermpropId(int)
     */
    private void setCvTermPropId(int cvTermPropId) {
        this.cvTermPropId = cvTermPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#getCvTermByCvTermId()
     */
    private CvTerm getCvTermByCvTermId() {
       return this.cvTermByCvTermId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#setCvTermByCvTermId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByCvTermId(CvTerm cvTermByCvTermId) {
        this.cvTermByCvTermId = cvTermByCvTermId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#getCvTermByTypeId()
     */
    private CvTerm getCvTermByTypeId() {
        return this.cvTermByTypeId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#setCvTermByTypeId(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTermByTypeId(CvTerm cvTermByTypeId) {
        this.cvTermByTypeId = cvTermByTypeId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#getRank()
     */
    private int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.CvTermPropI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }




}


