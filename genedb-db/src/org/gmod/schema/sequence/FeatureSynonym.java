package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.pub.Pub;



import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="feature_synonym")
public class FeatureSynonym implements Serializable {

    // Fields    
    @SequenceGenerator(name="generator", sequenceName="feature_synonym_feature_synonym_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    
    @Column(name="feature_synonym_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int featureSynonymId;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="synonym_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Synonym synonym;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Feature feature;
     
    @ManyToOne(cascade={}, fetch=FetchType.LAZY)
        
        @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Pub pub;
     
    @Column(name="is_current", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean current;
     
    @Column(name="is_internal", unique=false, nullable=false, insertable=true, updatable=true)
     private boolean internal;

     // Constructors

    /** default constructor */
    public FeatureSynonym() {
    }

    /** full constructor */
    public FeatureSynonym(Synonym synonym, Feature feature, Pub pub, boolean current, boolean internal) {
       this.synonym = synonym;
       this.feature = feature;
       this.pub = pub;
       this.current = current;
       this.internal = internal;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#getFeatureSynonymId()
     */
    public int getFeatureSynonymId() {
        return this.featureSynonymId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setFeatureSynonymId(int)
     */
    public void setFeatureSynonymId(int featureSynonymId) {
        this.featureSynonymId = featureSynonymId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#getSynonym()
     */
    public Synonym getSynonym() {
        return this.synonym;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setSynonym(org.gmod.schema.sequence.SynonymI)
     */
    public void setSynonym(Synonym synonym) {
        this.synonym = synonym;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#getFeature()
     */
    public Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setFeature(org.genedb.db.jpa.Feature)
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#getPub()
     */
    public Pub getPub() {
        return this.pub;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setPub(org.gmod.schema.pub.PubI)
     */
    public void setPub(Pub pub) {
        this.pub = pub;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#isCurrent()
     */
    public boolean isCurrent() {
        return this.current;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setCurrent(boolean)
     */
    public void setCurrent(boolean current) {
        this.current = current;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#isInternal()
     */
    public boolean isInternal() {
        return this.internal;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.FeatureSynonymI#setInternal(boolean)
     */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }




}


