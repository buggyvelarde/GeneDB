package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.Logger;

public class TopLevelFeatureMapper extends FeatureMapper {
    Logger logger = Logger.getLogger(TopLevelFeatureMapper.class);
    
    public static final String SQL = 
        " select uniquename, f.name as fname, seqlen, cvt.name as cvtname" +
        " from feature f, cvterm cvt " +
        " where f.feature_id = ?" +
        " and f.type_id = cvt.cvterm_id";
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        logger.debug("Enter mapRow");
        
        //Get transcript details 
        FeatureMapper mapper = new FeatureMapper(); 
        mapper.setUniqueName(rs.getString("uniquename"));
        mapper.setName(rs.getString("fname"));
        mapper.setSeqLen(rs.getInt("seqlen"));        
        mapper.setCvtName(rs.getString("cvtname"));
        logger.debug("Exit mapRow");
        return mapper;
    }
}
