package org.gmod.schema.sequence;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="feature_cvtermprop")
public class FeatureCvTermProp implements Serializable, PropertyI {

    // Fields    

     @Id
    @Column(name="feature_cvtermprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermPropId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;
     
     @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     private String value;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
     private int rank;

     // Constructors

    /** default constructor */
    public FeatureCvTermProp() {
    }

	/** minimal constructor */
    public FeatureCvTermProp(CvTerm cvTerm, FeatureCvTerm featureCvTerm, int rank) {
        this.cvTerm = cvTerm;
        this.featureCvTerm = featureCvTerm;
        this.rank = rank;
    }
    /** full constructor */
    public FeatureCvTermProp(CvTerm cvTerm, FeatureCvTerm featureCvTerm, String value, int rank) {
       this.cvTerm = cvTerm;
       this.featureCvTerm = featureCvTerm;
       this.value = value;
       this.rank = rank;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#getFeatureCvTermpropId()
     */
    private int getFeatureCvTermPropId() {
        return this.featureCvTermPropId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#setFeatureCvTermpropId(int)
     */
    private void setFeatureCvTermPropId(int featureCvTermPropId) {
        this.featureCvTermPropId = featureCvTermPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#getFeatureCvterm()
     */
    private FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#setFeatureCvterm(org.gmod.schema.sequence.FeatureCvTermI)
     */
    private void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#getRank()
     */
    private int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPropI#setRank(int)
     */
    private void setRank(int rank) {
        this.rank = rank;
    }




}


