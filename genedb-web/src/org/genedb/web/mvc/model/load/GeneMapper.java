package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class GeneMapper extends FeatureMapper {
    
    Logger logger = Logger.getLogger(GeneMapper.class);
    
    public static final String SQL = " select * " +
    " from feature f, featureloc fl " +
    " where f.feature_id = fl.feature_id" +
    " and f.type_id in (select cvterm_id from cvterm where name in ('gene', 'pseudogene'))";     
    
    public static final String SQL_WITH_PARAMS = " select * " +
    " from feature f, featureloc fl " +
    " where f.feature_id = fl.feature_id" +
    " and f.organism_id = ? " +
    " and f.type_id in (select cvterm_id from cvterm where name in ('gene', 'pseudogene'))";  
    

    
    public static final String SQL_WITH_LIMIT_AND_OFFSET_PARAMS = " select * " +
            " from feature f, featureloc fl " +
            " where f.feature_id = fl.feature_id" +
            " and f.organism_id = ? " +
            " and f.type_id in (select cvterm_id from cvterm where name in ('gene', 'pseudogene'))" +
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
        

        logger.debug("Exit mapRow");
        return gene;
    }

    
    
}
