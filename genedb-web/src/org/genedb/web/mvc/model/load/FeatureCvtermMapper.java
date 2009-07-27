package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.FeatureCvtermType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class FeatureCvtermMapper implements ParameterizedRowMapper<FeatureCvtermType>{
    
    public static final String SQL = "select fcvt.*, cvt.name as cvtname, cv.name as cvname, pub.uniquename as pubuniquename" +
            " from feature_cvterm fcvt, cvterm cvt, cv, pub" +
            " where fcvt.cvterm_id = cvt.cvterm_id" +
            " and fcvt.pub_id = pub.pub_id" +
            " and cvt.cv_id = cv.cv_id" +
            " and fcvt.feature_id = ?";

    @Override
    public FeatureCvtermType mapRow(ResultSet rs, int arg1)
            throws SQLException {
        FeatureCvtermType featureCvtermType = new FeatureCvtermType();
        featureCvtermType.setFeatureCvtId(rs.getInt("feature_cvterm_id"));
        featureCvtermType.setFeatureId(rs.getInt("feature_id"));
        featureCvtermType.setTypeId(rs.getInt("cvterm_id"));
        featureCvtermType.setPubId(rs.getInt("pub_id"));
        featureCvtermType.setCvtname(rs.getString("cvtname"));
        featureCvtermType.setCvname(rs.getString("cvname"));
        featureCvtermType.setPubUniqueName(rs.getString("pubuniquename"));
        return featureCvtermType;
    }
    
    
}
