package org.gmod.schema.analysis;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="analysis")
public class Analysis implements Serializable {

    // Fields    
     @Id
    
    @Column(name="analysis_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int analysisId;
     
     @Column(name="name", unique=false, nullable=true, insertable=true, updatable=true)
     private String name;
     
     @Column(name="description", unique=false, nullable=true, insertable=true, updatable=true)
     private String description;
     
     @Column(name="program", unique=false, nullable=false, insertable=true, updatable=true)
     private String program;
     
     @Column(name="programversion", unique=false, nullable=false, insertable=true, updatable=true)
     private String programVersion;
     
     @Column(name="algorithm", unique=false, nullable=true, insertable=true, updatable=true) 
     private String algorithm;
     
     @Column(name="sourcename", unique=false, nullable=true, insertable=true, updatable=true)
     private String sourceName;
     
     @Column(name="sourceversion", unique=false, nullable=true, insertable=true, updatable=true)
     private String sourceVersion;
     
     @Column(name="sourceuri", unique=false, nullable=true, insertable=true, updatable=true)
     private String sourceUri;
     
     @Column(name="timeexecuted", unique=false, nullable=false, insertable=true, updatable=true, length=29)
     private Date timeExecuted;
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="analysis")
     private Set<AnalysisFeature> analysisFeatures = new HashSet<AnalysisFeature>(0);
     
     @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="analysis")
     private Set<AnalysisProp> analysisProps = new HashSet<AnalysisProp>(0);

     // Constructors

    /** default constructor */
    private Analysis() {
    }

	/** minimal constructor */
    private Analysis(String program, String programVersion, Date timeExecuted) {
        this.program = program;
        this.programVersion = programVersion;
        this.timeExecuted = timeExecuted;
    }
    
    /** full constructor */
    private Analysis(String name, String description, String program, String programVersion, 
            String algorithm, String sourceName, String sourceVersion, String sourceUri, 
            Date timeExecuted, Set<AnalysisFeature> analysisFeatures, Set<AnalysisProp> analysisProps) {
       this.name = name;
       this.description = description;
       this.program = program;
       this.programVersion = programVersion;
       this.algorithm = algorithm;
       this.sourceName = sourceName;
       this.sourceVersion = sourceVersion;
       this.sourceUri = sourceUri;
       this.timeExecuted = timeExecuted;
       this.analysisFeatures = analysisFeatures;
       this.analysisProps = analysisProps;
    }
    
   
    // Property accessors
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getAnalysisId()
     */
    private int getAnalysisId() {
        return this.analysisId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setAnalysisId(int)
     */
    private void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getName()
     */
    private String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setName(java.lang.String)
     */
    private void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getDescription()
     */
    private String getDescription() {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setDescription(java.lang.String)
     */
    private void setDescription(String description) {
        this.description = description;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getProgram()
     */
    private String getProgram() {
        return this.program;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setProgram(java.lang.String)
     */
    private void setProgram(String program) {
        this.program = program;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getProgramversion()
     */
    private String getProgramVersion() {
        return this.programVersion;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setProgramversion(java.lang.String)
     */
    private void setProgramversion(String programVersion) {
        this.programVersion = programVersion;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getAlgorithm()
     */
    private String getAlgorithm() {
        return this.algorithm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setAlgorithm(java.lang.String)
     */
    private void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getSourcename()
     */
    private String getSourcename() {
        return this.sourceName;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setSourcename(java.lang.String)
     */
    private void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getSourceversion()
     */
    private String getSourceVersion() {
        return this.sourceVersion;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setSourceversion(java.lang.String)
     */
    private void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getSourceuri()
     */
    private String getSourceUri() {
        return this.sourceUri;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setSourceuri(java.lang.String)
     */
    private void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getTimeexecuted()
     */
    private Date getTimeExecuted() {
        return this.timeExecuted;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setTimeexecuted(java.util.Date)
     */
    private void setTimeexecuted(Date timeExecuted) {
        this.timeExecuted = timeExecuted;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getAnalysisfeatures()
     */
    private Set<AnalysisFeature> getAnalysisFeatures() {
        return this.analysisFeatures;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setAnalysisfeatures(java.util.Set)
     */
    private void setAnalysisFeatures(Set<AnalysisFeature> analysisFeatures) {
        this.analysisFeatures = analysisFeatures;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#getAnalysisprops()
     */
    private Set<AnalysisProp> getAnalysisProps() {
        return this.analysisProps;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.AnalysisI#setAnalysisprops(java.util.Set)
     */
    private void setAnalysisProps(Set<AnalysisProp> analysisProps) {
        this.analysisProps = analysisProps;
    }




}


