package org.gmod.schema.sequence;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.utils.propinterface.PropertyI;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.io.Serializable;
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
import javax.persistence.Table;

@Entity
@Table(name="featureprop")
@Indexed
public class FeatureProp implements Serializable, PropertyI {

    // Fields    
    @SuppressWarnings("unused")
    @GenericGenerator(name="generator", strategy="seqhilo", parameters = {  @Parameter(name="max_lo", value="100"), @Parameter(name="sequence", value="featureprop_featureprop_id_seq") } )
    @Id
    @GeneratedValue(generator="generator")
    @Column(name="featureprop_id", unique=false, nullable=false, insertable=true, updatable=true)
    @DocumentId
    private int featurePropId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
    @IndexedEmbedded(depth=1)
    public CvTerm cvTerm;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
    @IndexedEmbedded(depth=2)
    private Feature feature;
     
    @Column(name="value", unique=false, nullable=true, insertable=true, updatable=true)
    @Field(index = Index.TOKENIZED)
    private String value;
     
    @Column(name="rank", unique=false, nullable=false, insertable=true, updatable=true)
    private int rank;
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="featureprop")
    @ContainedIn
    private Set<FeaturePropPub> featurePropPubs = new HashSet<FeaturePropPub>(0);

     // Constructors
    /** default constructor */
    public FeatureProp() {
    }

	/** minimal constructor */
    private FeatureProp(CvTerm cvTerm, int rank) {
        this.cvTerm = cvTerm;
        this.rank = rank;
    }
    
    /** useful constructor ! */
    public FeatureProp(Feature feature, CvTerm cvTerm, String value, int rank) {
       this.cvTerm = cvTerm;
       this.feature = feature;
       this.value = value;
       this.rank = rank;
    }
    
    /** full constructor */
    private FeatureProp(CvTerm cvTerm, Feature feature, String value, int rank, Set<FeaturePropPub> featurePropPubs) {
       this.cvTerm = cvTerm;
       this.feature = feature;
       this.value = value;
       this.rank = rank;
       this.featurePropPubs = featurePropPubs;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#getFeature()
     */
    public Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#setFeature(org.genedb.db.jpa.Feature)
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#getValue()
     */
    public String getValue() {
        return this.value;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#getRank()
     */
    public int getRank() {
        return this.rank;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#setRank(int)
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#getFeaturepropPubs()
     */
    private Set<FeaturePropPub> getFeaturePropPubs() {
        return this.featurePropPubs;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeaturePropI#setFeaturepropPubs(java.util.Set)
     */
    public void setFeaturePropPubs(Set<FeaturePropPub> featurePropPubs) {
        this.featurePropPubs = featurePropPubs;
    }

    public int getFeaturePropId() {
        return this.featurePropId;
    }

    public void setFeaturePropId(final int featurePropId) {
        this.featurePropId = featurePropId;
    }

}


