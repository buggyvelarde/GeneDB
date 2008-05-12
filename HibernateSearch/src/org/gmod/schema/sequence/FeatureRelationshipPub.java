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
@Table(name="feature_relationship_pub")
//@Indexed
public class FeatureRelationshipPub implements Serializable {

    // Fields    
     @Id
    
    @Column(name="feature_relationship_pub_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int featureRelationshipPubId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private Pub pub;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private FeatureRelationship featureRelationship;

     // Constructors

    /** default constructor */
    public FeatureRelationshipPub() {
    }

    /** full constructor */
    private FeatureRelationshipPub(Pub pub, FeatureRelationship featureRelationship) {
       this.pub = pub;
       this.featureRelationship = featureRelationship;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#getFeatureRelationshipPubId()
     */
    private int getFeatureRelationshipPubId() {
        return this.featureRelationshipPubId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#setFeatureRelationshipPubId(int)
     */
    private void setFeatureRelationshipPubId(int featureRelationshipPubId) {
        this.featureRelationshipPubId = featureRelationshipPubId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#getPub()
     */
    private Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#setPub(org.gmod.schema.pub.PubI)
     */
    private void setPub(Pub pub) {
        this.pub = pub;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#getFeatureRelationship()
     */
    private FeatureRelationship getFeatureRelationship() {
        return this.featureRelationship;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipPubI#setFeatureRelationship(org.genedb.db.jpa.FeatureRelationship)
     */
    private void setFeatureRelationship(FeatureRelationship featureRelationship) {
        this.featureRelationship = featureRelationship;
    }




}


