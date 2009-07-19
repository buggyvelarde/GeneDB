package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class FeaturePropMapper implements
        ParameterizedRowMapper<FeaturePropMapper> {
    
    public static final String SQL = "select value " +
    		" from featureProp fp, cvterm cvt, cv" +
    		" where fp.type_id = cvt.cvterm_id" +
    		" and cvt.cv_id = cv.cv_id" +
    		" and feature_id = ?" +
    		" and cvt.name = ?" +
    		" and cv.name = ?";

    private String value;
    
    @Override
    public FeaturePropMapper mapRow(ResultSet rs, int rowCount)
            throws SQLException {
        FeaturePropMapper featurePropMapper = new FeaturePropMapper();
        featurePropMapper.setValue(rs.getString("value"));
        return featurePropMapper;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
