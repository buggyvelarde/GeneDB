package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;


public class FeatureMapper implements ParameterizedRowMapper<FeatureMapper> {
    Logger logger = Logger.getLogger(FeatureMapper.class);

    public String getResidues() {
        return residues;
    }

    public void setResidues(String residues) {
        this.residues = residues;
    }

    private int featureId;
    private int typeId;
    private int organismId;
    private String uniqueName;
    private String name;
    private int seqLen;
    private String residues;
    private Date timeLastModified;

    private int fmax;
    private int fmin;
    private int strand;
    private int sourceFeatureId;

    private String cvtName;
    private String cvName;

    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeatureMapper mapper = new FeatureMapper();
        mapper.setFeatureId(rs.getInt("feature_id"));
        mapper.setTypeId(rs.getInt("type_id"));
        mapper.setOrganismId(rs.getInt("organism_id"));
        mapper.setUniqueName(rs.getString("uniquename"));
        mapper.setName(rs.getString("name"));
        mapper.setSeqLen(rs.getInt("seqlen"));
        mapper.setResidues(rs.getString("residues"));
        mapper.setTimeLastModified(rs.getDate("timelastmodified"));
        return mapper;
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeqLen() {
        return seqLen;
    }

    public void setSeqLen(int seqLen) {
        this.seqLen = seqLen;
    }

    public Date getTimeLastModified() {
        return timeLastModified;
    }

    public void setTimeLastModified(Date timeLastModified) {
        this.timeLastModified = timeLastModified;
    }

    public int getFmax() {
        return fmax;
    }

    public void setFmax(int fmax) {
        this.fmax = fmax;
    }

    public int getFmin() {
        return fmin;
    }

    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

    public int getSourceFeatureId() {
        return sourceFeatureId;
    }

    public void setSourceFeatureId(int sourceFeatureId) {
        this.sourceFeatureId = sourceFeatureId;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getCvtName() {
        return cvtName;
    }

    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }

    public String getCvName() {
        return cvName;
    }

    public void setCvName(String cvName) {
        this.cvName = cvName;
    }

}
