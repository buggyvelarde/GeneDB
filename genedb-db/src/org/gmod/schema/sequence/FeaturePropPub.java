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
@Table(name="featureprop_pub")
public class FeaturePropPub implements Serializable {

    // Fields    
    @Id
    @Column(name="featureprop_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featurePropPubId;
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="featureprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureProp featureProp;
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;

     // Constructors

    /** default constructor */
    public FeaturePropPub() {
    }

    /** full constructor */
    private FeaturePropPub(FeatureProp featureProp, Pub pub) {
       this.featureProp = featureProp;
       this.pub = pub;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#getFeaturePropPubId()
     */
    private int getFeaturePropPubId() {
        return this.featurePropPubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#setFeaturePropPubId(int)
     */
    private void setFeaturePropPubId(int featurePropPubId) {
        this.featurePropPubId = featurePropPubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#getFeatureprop()
     */
    private FeatureProp getFeatureProp() {
        return this.featureProp;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#setFeatureprop(org.genedb.db.jpa.FeatureProp)
     */
    private void setFeatureProp(FeatureProp featureProp) {
        this.featureProp = featureProp;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropPubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }




}


