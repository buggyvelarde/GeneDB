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
@Table(name="featureloc_pub")
public class FeatureLocPub implements Serializable {

    // Fields    
    @Id
    @Column(name="featureloc_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureLocPubId;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
    @JoinColumn(name="featureloc_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureLoc featureLoc;
     
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
    @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;

    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#getFeatureLocPubId()
     */
    private int getFeatureLocPubId() {
        return this.featureLocPubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#setFeatureLocPubId(int)
     */
    private void setFeatureLocPubId(int featureLocPubId) {
        this.featureLocPubId = featureLocPubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#getFeatureloc()
     */
    private FeatureLoc getFeatureloc() {
        return this.featureLoc;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#setFeatureloc(org.gmod.schema.sequence.FeatureLocI)
     */
    private void setFeatureloc(FeatureLoc featureloc) {
        this.featureLoc = featureloc;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureLocPubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }




}


