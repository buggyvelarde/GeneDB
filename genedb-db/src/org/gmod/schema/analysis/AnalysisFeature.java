package org.gmod.schema.analysis;

import org.gmod.schema.sequence.Feature;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="analysisfeature")
public class AnalysisFeature implements Serializable {

    // Fields    

    @Id
    @Column(name="analysisfeature_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int analysisFeatureId;
    
    @ManyToOne(cascade={},fetch=FetchType.LAZY)
        @JoinColumn(name="analysis_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Analysis analysis;
     
     @ManyToOne(cascade={}, fetch=FetchType.LAZY)
     @JoinColumn(name="feature_id", unique=false, nullable=false, insertable=true, updatable=true)
     private Feature feature;
     
     @Column(name="rawscore", unique=false, nullable=true, insertable=true, updatable=true, precision=17, scale=17)
     private Double rawScore;
     
     @Column(name="normscore", unique=false, nullable=true, insertable=true, updatable=true, precision=17, scale=17)
     private Double normScore;
     
     @Column(name="significance", unique=false, nullable=true, insertable=true, updatable=true, precision=17, scale=17)
     private Double significance;
     
     @Column(name="identity", unique=false, nullable=true, insertable=true, updatable=true, precision=17, scale=17)
     private Double identity;

     // Constructors

    /** default constructor */
    private AnalysisFeature() {
    }

	/** minimal constructor */
    private AnalysisFeature(Analysis analysis, Feature feature) {
        this.analysis = analysis;
        this.feature = feature;
    }
    /** full constructor */
    private AnalysisFeature(Analysis analysis, Feature feature, Double rawScore, Double normScore, Double significance, Double identity) {
       this.analysis = analysis;
       this.feature = feature;
       this.rawScore = rawScore;
       this.normScore = normScore;
       this.significance = significance;
       this.identity = identity;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getAnalysisFeatureId()
     */
    private int getAnalysisFeatureId() {
        return this.analysisFeatureId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setAnalysisFeatureId(int)
     */
    private void setAnalysisFeatureId(int analysisFeatureId) {
        this.analysisFeatureId = analysisFeatureId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getAnalysis()
     */
    private Analysis getAnalysis() {
        return this.analysis;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setAnalysis(org.genedb.db.jpa.Analysis)
     */
    private void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getFeature()
     */
    private Feature getFeature() {
        return this.feature;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setFeature(org.genedb.db.jpa.Feature)
     */
    private void setFeature(Feature feature) {
        this.feature = feature;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getRawscore()
     */
    private Double getRawscore() {
        return this.rawScore;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setRawscore(java.lang.Double)
     */
    private void setRawscore(Double rawScore) {
        this.rawScore = rawScore;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getNormscore()
     */
    private Double getNormScore() {
        return this.normScore;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setNormscore(java.lang.Double)
     */
    private void setNormScore(Double normScore) {
        this.normScore = normScore;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getSignificance()
     */
    private Double getSignificance() {
        return this.significance;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setSignificance(java.lang.Double)
     */
    private void setSignificance(Double significance) {
        this.significance = significance;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#getIdentity()
     */
    private Double getIdentity() {
        return this.identity;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisFeatureI#setIdentity(java.lang.Double)
     */
    private void setIdentity(Double identity) {
        this.identity = identity;
    }




}


