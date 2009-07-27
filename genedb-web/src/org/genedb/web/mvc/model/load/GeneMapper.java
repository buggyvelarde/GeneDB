package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class GeneMapper extends FeatureMapper {
    
    Logger logger = Logger.getLogger(GeneMapper.class);
    
    public static final String GENE_TYPE_SQL = 
        " select cvterm_id " +
        " from cvterm cvt, cv " +
        " where cvt.cv_id = cv.cv_id " +
        " and cvt.name in ('gene', 'pseudogene')" +
        " and cv.name = 'sequence'";
    
    public static final String SQL = 
        " select fl.*, f.*, cvt.name as cvtname, cv.name as cvname " +
        " from feature f, featureloc fl, cvterm cvt, cv " +
        " where f.feature_id = fl.feature_id" +
        " and f.type_id = cvt.cvterm_id " +
        " and cvt.cv_id = cv.cv_id " +
        " and f.type_id in (" + GENE_TYPE_SQL + " )";     
    
    public static final String SQL_WITH_PARAMS = 
        " select fl.*, f.*, cvt.name as cvtname, cv.name as cvname " +
        " from feature f, featureloc fl, cvterm cvt, cv " +
        " where f.feature_id = fl.feature_id" +
        " and f.type_id = cvt.cvterm_id " +
        " and cvt.cv_id = cv.cv_id " +
        " and f.organism_id = ? " +
        " and f.type_id in (" + GENE_TYPE_SQL + ")";  
    

    
    public static final String SQL_WITH_LIMIT_AND_OFFSET_PARAMS = 
            " select fl.*, f.*, cvt.name as cvtname, cv.name as cvname " +
            " from feature f, featureloc fl, cvterm cvt, cv " +
            " where f.feature_id = fl.feature_id" +
            " and f.type_id = cvt.cvterm_id " +
            " and cvt.cv_id = cv.cv_id " +
            " and f.organism_id = ? " +
            " and f.type_id in (" + GENE_TYPE_SQL + ")" +
            " limit ?" +
            " offset ?";
    
    private SimpleJdbcTemplate template;
    
    
    public GeneMapper(SimpleJdbcTemplate template){
        this.template = template;
    }
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        logger.debug("Enter mapRow");        
        FeatureMapper gene = super.mapRow(rs, rowNum);
        gene.setFmax(rs.getInt("fmax"));
        gene.setFmin(rs.getInt("fmin"));
        gene.setSourceFeatureId(rs.getInt("srcfeature_id"));
        gene.setStrand(rs.getInt("strand"));
        gene.setCvtName(rs.getString("cvtname"));
        gene.setCvName(rs.getString("cvname"));        

        logger.debug("Exit mapRow");
        return gene;
    }

    
    
}
