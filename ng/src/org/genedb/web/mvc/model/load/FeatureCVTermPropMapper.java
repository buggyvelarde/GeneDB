package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.FeatureCVTPropType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class FeatureCVTermPropMapper implements ParameterizedRowMapper<FeatureCVTPropType>{
    
    public static final String SQL = 
        " select fcvtp.value, cvt.name " +
        " from feature_cvtermprop fcvtp, cvterm cvt " +
        " where fcvtp.type_id = cvt.cvterm_id " +
        " and fcvtp.feature_cvterm_id = ?";

    @Override
    public FeatureCVTPropType mapRow(ResultSet rs, int rowCount) throws SQLException {
        return new FeatureCVTPropType(
                rs.getString("name"), rs.getString("value"));
    }

}
