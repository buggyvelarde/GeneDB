package org.genedb.web.mvc.model.simple;

import org.genedb.web.mvc.model.load.FeatureMapper;
import org.genedb.web.mvc.model.load.TopLevelFeatureMapper;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleTopLevelFeatureMapper extends FeatureMapper {
    Logger logger = Logger.getLogger(TopLevelFeatureMapper.class);

    public static final String SQL =
            " select uniquename, f.name as fname, seqlen, cvt.name as cvtname" + " from feature f, cvterm cvt "
                    + " where f.feature_id = ?" + " and f.type_id = cvt.cvterm_id";

    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Get transcript details
        SimpleTopLevelFeatureMapper mapper = new SimpleTopLevelFeatureMapper();
        mapper.setUniqueName(rs.getString("uniquename"));
        mapper.setName(rs.getString("fname"));
        mapper.setSeqLen(rs.getInt("seqlen"));
        mapper.setCvtName(rs.getString("cvtname"));
        return mapper;
    }
}
