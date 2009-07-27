package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class FeatureCvtermMapper implements ParameterizedRowMapper<FeatureCvtermMapper>{
    
    private int featureCvtId;
    private int featureId;
    private int typeId;
    private int pubId;


    public static final String SQL = "select fcvt.*" +
            " from feature_cvterm fcvt, cvterm cvt" +
            " where fcvt.cvterm_id = cvt.cvterm_id" +
            " and fcvt.feature_id = ?";


    public int getFeatureCvtId() {
        return featureCvtId;
    }

    public void setFeatureCvtId(int featureCvtId) {
        this.featureCvtId = featureCvtId;
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

    public static String getSQL() {
        return SQL;
    }
    
    public int getPubId() {
        return pubId;
    }

    public void setPubId(int pubId) {
        this.pubId = pubId;
    }

    @Override
    public FeatureCvtermMapper mapRow(ResultSet rs, int arg1)
            throws SQLException {
        FeatureCvtermMapper featureCvtermMapper = new FeatureCvtermMapper();
        featureCvtermMapper.setFeatureCvtId(rs.getInt("feature_cvterm_id"));
        featureCvtermMapper.setFeatureId(rs.getInt("feature_id"));
        featureCvtermMapper.setTypeId(rs.getInt("cvterm_id"));
        featureCvtermMapper.setPubId(rs.getInt("pub_id"));
        return featureCvtermMapper;
    }
    
    
}
