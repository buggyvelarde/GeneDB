package org.gmod.schema.sequence;



import org.gmod.schema.pub.Pub;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

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
@Indexed
public class FeaturePub implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int featurePubId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private Feature feature;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private Pub pub;

     // Constructors

    /** default constructor */
    public FeaturePub() {
    }

    /** full constructor */
    public FeaturePub(Feature feature, Pub pub) {
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
    public Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#setFeature(org.genedb.db.jpa.Feature)
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#getPub()
     */
    public Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePubI#setPub(org.gmod.schema.pub.PubI)
     */
    public void setPub(Pub pub) {
        this.pub = pub;
    }




}


