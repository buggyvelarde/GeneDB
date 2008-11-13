package org.gmod.schema.mapped;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "analysis")
public class Analysis implements Serializable {

    // Fields
    @SequenceGenerator(name = "generator", sequenceName = "analysis_analysis_id_seq")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "analysis_id", unique = false, nullable = false, insertable = true, updatable = true)
    private int analysisId;

    @Column(name = "name", unique = false, nullable = true, insertable = true, updatable = true)
    private String name;

    @Column(name = "description", unique = false, nullable = true, insertable = true, updatable = true)
    private String description;

    @Column(name = "program", unique = false, nullable = false, insertable = true, updatable = true)
    private String program;

    @Column(name = "programversion", unique = false, nullable = false, insertable = true, updatable = true)
    private String programVersion;

    @Column(name = "algorithm", unique = false, nullable = true, insertable = true, updatable = true)
    private String algorithm;

    @Column(name = "sourcename", unique = false, nullable = true, insertable = true, updatable = true)
    private String sourceName;

    @Column(name = "sourceversion", unique = false, nullable = true, insertable = true, updatable = true)
    private String sourceVersion;

    @Column(name = "sourceuri", unique = false, nullable = true, insertable = true, updatable = true)
    private String sourceUri;

    @Column(name = "timeexecuted", unique = false, nullable = false, insertable = true, updatable = true, length = 29)
    private Date timeExecuted;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "analysis")
    private Collection<AnalysisFeature> analysisFeatures;

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "analysis")
    private Collection<AnalysisProp> analysisProps;

    // Constructors
    public Analysis() {
        timeExecuted = new Date();
    }

    // Property accessors
    public int getAnalysisId() {
        return this.analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProgram() {
        return this.program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgramVersion() {
        return this.programVersion;
    }

    public void setProgramVersion(String programVersion) {
        this.programVersion = programVersion;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceVersion() {
        return this.sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public String getSourceUri() {
        return this.sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public Date getTimeExecuted() {
        return this.timeExecuted;
    }

    public void setTimeExecuted(Date timeExecuted) {
        this.timeExecuted = timeExecuted;
    }

    public Collection<AnalysisFeature> getAnalysisFeatures() {
        return this.analysisFeatures;
    }

    public void setAnalysisFeatures(Collection<AnalysisFeature> analysisFeatures) {
        this.analysisFeatures = analysisFeatures;
    }

    public Collection<AnalysisProp> getAnalysisProps() {
        return Collections.unmodifiableCollection(this.analysisProps);
    }

    public void addAnalysisProp(AnalysisProp analysisProp) {
        this.analysisProps.add(analysisProp);
        analysisProp.setAnalysis(this);
    }
}
