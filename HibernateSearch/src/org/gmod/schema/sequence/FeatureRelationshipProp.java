package org.gmod.schema.sequence;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="feature_relationshipprop")
//@Indexed
public class FeatureRelationshipProp implements Serializable, PropertyI {

    // Fields    
     @Id
    
    @Column(name="feature_relationshipprop_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
     private int featureRelationshipPropId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private CvTerm cvTerm;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
     @IndexedEmbedded(depth=1)
     private FeatureRelationship featureRelationship;
     
     @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
     @Field(index = Index.TOKENIZED)
     private String value;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)     
     private int rank;
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureRelationshipprop")
     @ContainedIn
     private Set<FeatureRelationshipPropPub> featureRelationshipPropPubs = new HashSet<FeatureRelationshipPropPub>(0);

     // Constructors

    /** default constructor */
    public FeatureRelationshipProp() {
    }

	/** minimal constructor */
    private FeatureRelationshipProp(CvTerm cvTerm, FeatureRelationship featureRelationship, int rank) {
        this.cvTerm = cvTerm;
        this.featureRelationship = featureRelationship;
        this.rank = rank;
    }
    /** full constructor */
    private FeatureRelationshipProp(CvTerm cvTerm, FeatureRelationship featureRelationship, String value, int rank, Set<FeatureRelationshipPropPub> featureRelationshipPropPubs) {
       this.cvTerm = cvTerm;
       this.featureRelationship = featureRelationship;
       this.value = value;
       this.rank = rank;
       this.featureRelationshipPropPubs = featureRelationshipPropPubs;
    }
    
   
    // Property accessors

    private int getFeatureRelationshipPropId() {
        return this.featureRelationshipPropId;
    }
    
    private void setFeatureRelationshipPropId(int featureRelationshipPropId) {
        this.featureRelationshipPropId = featureRelationshipPropId;
    }

    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    private void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    private FeatureRelationship getFeatureRelationship() {
        return this.featureRelationship;
    }
    
    private void setFeatureRelationship(FeatureRelationship featureRelationship) {
        this.featureRelationship = featureRelationship;
    }
    

    private String getValue() {
        return this.value;
    }
    
    private void setValue(String value) {
        this.value = value;
    }
    

    private int getRank() {
        return this.rank;
    }
    
    private void setRank(int rank) {
        this.rank = rank;
    }

    private Collection<FeatureRelationshipPropPub> getFeatureRelationshipPropPubs() {
        return this.featureRelationshipPropPubs;
    }
    
    private void setFeatureRelationshipPropPubs(Set<FeatureRelationshipPropPub> featureRelationshipPropPubs) {
        this.featureRelationshipPropPubs = featureRelationshipPropPubs;
    }




}


