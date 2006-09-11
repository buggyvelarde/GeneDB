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
@Table(name="feature_pub")
public class FeaturePub implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featurePubId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Feature feature;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;

     // Constructors

    /** default constructor */
    public FeaturePub() {
    }

    /** full constructor */
    private FeaturePub(Feature feature, Pub pub) {
       this.feature = feature;
       this.pub = pub;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#getFeaturePubId()
     */
    private int getFeaturePubId() {
        return this.featurePubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#setFeaturePubId(int)
     */
    private void setFeaturePubId(int featurePubId) {
        this.featurePubId = featurePubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#getFeature()
     */
    private Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#setFeature(org.genedb.db.jpa.Feature)
     */
    private void setFeature(Feature feature) {
        this.feature = feature;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }




}


