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
@Table(name="feature_relationshipprop_pub")
public class FeatureRelationshipPropPub implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_relationshipprop_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureRelationshipPropPubId;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_relationshipprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private FeatureRelationshipProp featureRelationshipProp;
     
     @ManyToOne(cascade={},
             fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;

     // Constructors

    /** default constructor */
    public FeatureRelationshipPropPub() {
    }

    /** full constructor */
    private FeatureRelationshipPropPub(FeatureRelationshipProp featureRelationshipProp, Pub pub) {
       this.featureRelationshipProp = featureRelationshipProp;
       this.pub = pub;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#getFeatureRelationshipPropPubId()
     */
    private int getFeatureRelationshipPropPubId() {
        return this.featureRelationshipPropPubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#setFeatureRelationshipPropPubId(int)
     */
    private void setFeatureRelationshipPropPubId(int featureRelationshipPropPubId) {
        this.featureRelationshipPropPubId = featureRelationshipPropPubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#getFeatureRelationshipprop()
     */
    private FeatureRelationshipProp getFeatureRelationshipProp() {
        return this.featureRelationshipProp;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#setFeatureRelationshipprop(org.genedb.db.jpa.FeatureRelationshipProp)
     */
    private void setFeatureRelationshipProp(FeatureRelationshipProp featureRelationshipProp) {
        this.featureRelationshipProp = featureRelationshipProp;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPropPubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }




}


