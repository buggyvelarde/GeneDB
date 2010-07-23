package org.gmod.schema.mapped;


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
@Table(name="analysisfeature")
public class AnalysisFeature implements Serializable {

    // Fields

    @SequenceGenerator(name="generator",sequenceName="analysisfeature_analysisfeature_id_seq" )
    @Id @GeneratedValue(generator="generator")
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
    public AnalysisFeature() {
        // Deliberately empty default constructor
    }

    /** minimal constructor */
    public AnalysisFeature(Analysis analysis, Feature feature) {
        this.analysis = analysis;
        this.feature = feature;
    }
    /** full constructor */
    public AnalysisFeature(Analysis analysis, Feature feature, Double rawScore, Double normScore, Double significance, Double identity) {
       this.analysis = analysis;
       this.feature = feature;
       this.rawScore = rawScore;
       this.normScore = normScore;
       this.significance = significance;
       this.identity = identity;
    }


    // Property accessors

    public int getAnalysisFeatureId() {
        return this.analysisFeatureId;
    }

    public void setAnalysisFeatureId(int analysisFeatureId) {
        this.analysisFeatureId = analysisFeatureId;
    }

    public Analysis getAnalysis() {
        return this.analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Double getRawScore() {
        return this.rawScore;
    }

    public void setRawScore(Double rawScore) {
        this.rawScore = rawScore;
    }

    public Double getNormScore() {
        return this.normScore;
    }

    public void setNormScore(Double normScore) {
        this.normScore = normScore;
    }

    public Double getSignificance() {
        return this.significance;
    }

    public void setSignificance(Double significance) {
        this.significance = significance;
    }

    public Double getIdentity() {
        return this.identity;
    }

    public void setIdentity(Double identity) {
        this.identity = identity;
    }
}
