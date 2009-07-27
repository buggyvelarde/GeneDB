package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.FeaturePropType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class FeaturePropMapper implements
        ParameterizedRowMapper<FeaturePropType> {
    
//    public static final String SQL = "select value " +
//    		" from featureProp fp, cvterm cvt, cv" +
//    		" where fp.type_id = cvt.cvterm_id" +
//    		" and cvt.cv_id = cv.cv_id" +
//    		" and feature_id = ?" +
//    		" and cvt.name = ?" +
//    		" and cv.name = ?";
    
    public static final String SQL = 
            "select featureprop_id, value, cvt.name as cvtname, cv.name as cvname " +
            " from featureProp fp, cvterm cvt, cv" +
            " where fp.type_id = cvt.cvterm_id" +
            " and cvt.cv_id = cv.cv_id" +
            " and feature_id = ?" ;

    private int featurePropId;
    private String value;
    private String cvtName;
    private String cvName;
    
    @Override
    public FeaturePropType mapRow(ResultSet rs, int rowCount)
            throws SQLException {
        FeaturePropType featurePropType = new FeaturePropType();
        featurePropType.setFeaturePropId(rs.getInt("featureprop_id"));
        featurePropType.setValue(rs.getString("value"));
        featurePropType.setCvtName(rs.getString("cvtname"));
        featurePropType.setCvName(rs.getString("cvname"));
        return featurePropType;
    }    

    public int getFeaturePropId() {
        return featurePropId;
    }

    public void setFeaturePropId(int featurePropId) {
        this.featurePropId = featurePropId;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
