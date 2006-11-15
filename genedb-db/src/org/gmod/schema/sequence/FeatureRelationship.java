package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;

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
@Table(name="feature_relationship")
public class FeatureRelationship implements Serializable {

    // Fields    

    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    @Column(name="feature_relationship_id", unique=false, nullable=false, insertable=true, updatable=true)
    @SequenceGenerator(name="generator", sequenceName="feature_relationship_feature_relationship_id_seq")
    private int featureRelationshipId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="subject_id", unique=false, nullable=false, insertable=true, updatable=true)     
    private Feature featureBySubjectId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="object_id", unique=false, nullable=false, insertable=true, updatable=true) 
    private Feature featureByObjectId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)    
     private String value;
     
    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)     
     private int rank;
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureRelationship")
     private Set<FeatureRelationshipProp> featureRelationshipProps = new HashSet<FeatureRelationshipProp>(0);
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureRelationship")
     private Set<FeatureRelationshipPub> featureRelationshipPubs = new HashSet<FeatureRelationshipPub>(0);

     // Constructors

    /** default constructor */
    public FeatureRelationship() {
    }

	/** minimal constructor */
    public FeatureRelationship(Feature featureBySubjectId, Feature featureByObjectId, CvTerm cvTerm, int rank) {
        this.featureBySubjectId = featureBySubjectId;
        this.featureByObjectId = featureByObjectId;
        this.cvTerm = cvTerm;
        this.rank = rank;
    }
    /** full constructor */
    private FeatureRelationship(Feature featureBySubjectId, Feature featureByObjectId, CvTerm cvTerm, String value, int rank, Set<FeatureRelationshipProp> featureRelationshipProps, Set<FeatureRelationshipPub> featureRelationshipPubs) {
       this.featureBySubjectId = featureBySubjectId;
       this.featureByObjectId = featureByObjectId;
       this.cvTerm = cvTerm;
       this.value = value;
       this.rank = rank;
       this.featureRelationshipProps = featureRelationshipProps;
       this.featureRelationshipPubs = featureRelationshipPubs;
    }
    
   
    // Property accessors


    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getFeatureRelationshipId()
     */
    private int getFeatureRelationshipId() {
        return this.featureRelationshipId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setFeatureRelationshipId(int)
     */
    private void setFeatureRelationshipId(int featureRelationshipId) {
        this.featureRelationshipId = featureRelationshipId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getFeatureBySubjectId()
     */
    public Feature getFeatureBySubjectId() {
        return this.featureBySubjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setFeatureBySubjectId(org.genedb.db.jpa.Feature)
     */
    public void setFeatureBySubjectId(Feature featureBySubjectId) {
        this.featureBySubjectId = featureBySubjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getFeatureByObjectId()
     */
    public Feature getFeatureByObjectId() {
        return this.featureByObjectId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setFeatureByObjectId(org.genedb.db.jpa.Feature)
     */
    public void setFeatureByObjectId(Feature featureByObjectId) {
        this.featureByObjectId = featureByObjectId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getValue()
     */
    public String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setValue(java.lang.String)
     */
    private void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getRank()
     */
    public int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setRank(int)
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getFeatureRelationshipprops()
     */
    private Collection<FeatureRelationshipProp> getFeatureRelationshipProps() {
        return this.featureRelationshipProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setFeatureRelationshipprops(java.util.Set)
     */
    private void setFeatureRelationshipProps(Set<FeatureRelationshipProp> featureRelationshipProps) {
        this.featureRelationshipProps = featureRelationshipProps;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#getFeatureRelationshipPubs()
     */
    private Collection<FeatureRelationshipPub> getFeatureRelationshipPubs() {
        return this.featureRelationshipPubs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureRelationshipI#setFeatureRelationshipPubs(java.util.Set)
     */
    private void setFeatureRelationshipPubs(Set<FeatureRelationshipPub> featureRelationshipPubs) {
        this.featureRelationshipPubs = featureRelationshipPubs;
    }




}


