package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="feature_relationshipprop")
public class FeatureRelationshipProp implements Serializable, PropertyI {

    // Fields    
	@SequenceGenerator(name="generator", sequenceName="feature_relationshipprop_feature_relationshipprop_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_relationshipprop_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureRelationshipPropId;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
         
         @JoinColumn(name="feature_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)    
     private FeatureRelationship featureRelationship;
     
     @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)     
     private String value;
     
     @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)     
     private int rank;
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureRelationshipProp")     
     private Collection<FeatureRelationshipPropPub> featureRelationshipPropPubs;
    
   
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
    
    private void setFeatureRelationshipPropPubs(Collection<FeatureRelationshipPropPub> featureRelationshipPropPubs) {
        this.featureRelationshipPropPubs = featureRelationshipPropPubs;
    }




}


