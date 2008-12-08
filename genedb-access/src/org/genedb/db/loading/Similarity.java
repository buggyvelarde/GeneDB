package org.genedb.db.loading;

import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.utils.SimilarityI;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a similarity record (corresponding to a /similarity qualifier in a PSU "embl" file).
 * Just a data-holder really.
 *
 * @author rh11
 *
 */
public class Similarity implements SimilarityI {
    private String analysisProgram;
    private String analysisProgramVersion = "";
    private Analysis analysis; // If this is set, it's used instead of analysisProgram and analysisProgramVersion

    private String organismName;
    private String geneName;
    private String product;
    private DbXRef primaryDbXRef;
    private Collection<DbXRef> secondaryDbXRefs = new ArrayList<DbXRef>();
    private int length;
    private Double rawScore;
    private Double eValue;
    private int overlap;
    private int queryStart = 0, queryEnd = -1;
    private int targetStart = 0, targetEnd = -1;
    private Double identity, ungappedId;
    private String uniqueIdentifier;

    public String getAnalysisProgram() {
        return analysisProgram;
    }
    public void setAnalysisProgram(String analysisProgram) {
        this.analysisProgram = analysisProgram;
    }
    public String getAnalysisProgramVersion() {
        return analysisProgramVersion;
    }
    public void setAnalysisProgramVersion(String analysisProgramVersion) {
        this.analysisProgramVersion = analysisProgramVersion;
    }
    public Analysis getAnalysis() {
        return analysis;
    }
    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }
    public String getOrganismName() {
        return organismName;
    }
    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }
    public String getGeneName() {
        return geneName;
    }
    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public DbXRef getPrimaryDbXRef() {
        return primaryDbXRef;
    }
    public void setPrimaryDbXRef(DbXRef primaryDbXRef) {
        this.primaryDbXRef = primaryDbXRef;
    }
    public Collection<DbXRef> getSecondaryDbXRefs() {
        return secondaryDbXRefs;
    }
    public void addDbXRef(DbXRef dbXRef) {
        this.secondaryDbXRefs.add(dbXRef);
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public Double getRawScore() {
        return rawScore;
    }
    public void setRawScore(Double rawScore) {
        this.rawScore = rawScore;
    }
    public Double getEValue() {
        return eValue;
    }
    public void setEValue(Double eValue) {
        this.eValue = eValue;
    }
    public int getOverlap() {
        return overlap;
    }
    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }
    public int getQueryStart() {
        return queryStart;
    }
    public void setQueryStart(int queryStart) {
        this.queryStart = queryStart;
    }
    public int getQueryEnd() {
        return queryEnd;
    }
    public void setQueryEnd(int queryEnd) {
        this.queryEnd = queryEnd;
    }
    public int getTargetStart() {
        return targetStart;
    }
    public void setTargetStart(int targetStart) {
        this.targetStart = targetStart;
    }
    public int getTargetEnd() {
        return targetEnd;
    }
    public void setTargetEnd(int targetEnd) {
        this.targetEnd = targetEnd;
    }
    public Double getId() {
        return identity;
    }
    public void setId(Double id) throws DataError {
        if (id != null && (id < 0.0 || id > 100.0)) {
            throw new DataError(String.format("id percentage (%g) must be between 0 and 100", id));
        }
        this.identity = id;
    }
    public Double getUngappedId() {
        return ungappedId;
    }
    public void setUngappedId(Double ungappedId) throws DataError {
        if (ungappedId != null && (ungappedId < 0.0 || ungappedId > 100.0)) {
            throw new DataError(String.format("ungapped id percentage (%g) must be between 0 and 100", ungappedId));
        }
        this.ungappedId = ungappedId;
    }
    public String getUniqueIdentifier() {
        if (uniqueIdentifier == null) {
            throw new IllegalStateException("The unique identifier has not been set!");
        }
        return uniqueIdentifier;
    }
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String toString() {
        return String.format("%s (analysisProgram=%s, version=%s; analysis=%s; " +
                        "organism=%s; gene=%s; product=%s; dbxref=%s (%s); length=%d; raw score=%s; e=%s; " +
                        "overlap=%d; query=%d-%d; target=%d-%d; identity=%s; ungapped id=%s)",
            uniqueIdentifier,
            analysisProgram, analysisProgramVersion, analysis, organismName, geneName, product,
            primaryDbXRef, secondaryDbXRefs, length, rawScore, eValue, overlap, queryStart, queryEnd,
            targetStart, targetEnd, identity, ungappedId);
    }
}
