package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeatureTypeMapper extends FeatureMapper{
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeatureMapper featureMapper = super.mapRow(rs, rowNum);
        featureMapper.setCvtName(rs.getString("name"));
        return featureMapper;
    }

}
