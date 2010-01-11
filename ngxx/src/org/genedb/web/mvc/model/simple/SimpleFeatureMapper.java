package org.genedb.web.mvc.model.simple;

import org.genedb.web.mvc.model.load.FeatureMapper;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleFeatureMapper {
    Logger logger = Logger.getLogger(FeatureMapper.class);

    public void mapRow(SimpleFeature feature, ResultSet rs) throws SQLException {
        feature.setFeatureId(rs.getInt("feature_id"));
        feature.setUniqueName(rs.getString("uniquename"));
    }

}
