package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import common.Logger;

public class TranscriptMapper extends FeatureMapper {
    Logger logger = Logger.getLogger(TranscriptMapper.class);
    
    public static final String SQL = " select f.*, cvt.name as cvtname, cv.name as cvname " +
    "from feature f, feature_relationship fr, cvterm cvt, cv " +
    " where fr.subject_id = f.feature_id " +
    " and f.type_id = cvt.cvterm_id" +
    " and cvt.cv_id = cv.cv_id" +    
    " and fr.object_id = ?";
    
    
    private SimpleJdbcTemplate template;
    
    public TranscriptMapper(SimpleJdbcTemplate template){
        this.template = template;
    }
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        logger.debug("Enter mapRow");
        
        //Get transcript details 
        FeatureMapper transcriptMapper = super.mapRow(rs, rowNum);          
        transcriptMapper.setCvtName(rs.getString("cvtname"));
        transcriptMapper.setCvName(rs.getString("cvname"));

        logger.debug("Exit mapRow");
        return transcriptMapper;
    }
}
