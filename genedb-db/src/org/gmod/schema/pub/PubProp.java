package org.gmod.schema.pub;

import org.genedb.db.propinterface.PropertyI;

import org.gmod.schema.cv.CvTerm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="pubprop")
public class PubProp implements Serializable, PropertyI {

    // Fields    
     @Id
    
    @Column(name="pubprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int pubPropId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;
     
     @Column(name="value", unique=false, nullable=false, insertable=true, updatable=true)
     private String value;
     
     @Column(name="rank", unique=false, nullable=true, insertable=true, updatable=true)
     private Integer rank;

     // Constructors

    /** default constructor */
    public PubProp() {
    }

	/** minimal constructor */
    private PubProp(CvTerm cvTerm, Pub pub, String value) {
        this.cvTerm = cvTerm;
        this.pub = pub;
        this.value = value;
    }
    /** full constructor */
    private PubProp(CvTerm cvTerm, Pub pub, String value, Integer rank) {
       this.cvTerm = cvTerm;
       this.pub = pub;
       this.value = value;
       this.rank = rank;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#getPubPropId()
     */
    private int getPubPropId() {
        return this.pubPropId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#setPubPropId(int)
     */
    private void setPubPropId(int pubPropId) {
        this.pubPropId = pubPropId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#getCvTerm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#setCvTerm(org.gmod.schema.cv.CvTermI)
     */
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#getValue()
     */
    private String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#getRank()
     */
    private Integer getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.PubPropI#setRank(java.lang.Integer)
     */
    private void setRank(Integer rank) {
        this.rank = rank;
    }




}


