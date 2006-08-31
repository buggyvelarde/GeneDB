package org.gmod.schema.sequence;



import org.gmod.schema.pub.Pub;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="feature_cvterm_pub")
public class FeatureCvTermPub implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_cvterm_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureCvTermPubId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_cvterm_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureCvTerm featureCvTerm;

     // Constructors

    /** default constructor */
    private FeatureCvTermPub() {
    }

    /** full constructor */
    private FeatureCvTermPub(Pub pub, FeatureCvTerm featureCvTerm) {
       this.pub = pub;
       this.featureCvTerm = featureCvTerm;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#getFeatureCvTermPubId()
     */
    private int getFeatureCvTermPubId() {
        return this.featureCvTermPubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#setFeatureCvTermPubId(int)
     */
    private void setFeatureCvTermPubId(int featureCvTermPubId) {
        this.featureCvTermPubId = featureCvTermPubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#getFeatureCvterm()
     */
    private FeatureCvTerm getFeatureCvTerm() {
        return this.featureCvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureCvTermPubI#setFeatureCvterm(org.gmod.schema.sequence.FeatureCvTermI)
     */
    private void setFeatureCvTerm(FeatureCvTerm featureCvTerm) {
        this.featureCvTerm = featureCvTerm;
    }




}


