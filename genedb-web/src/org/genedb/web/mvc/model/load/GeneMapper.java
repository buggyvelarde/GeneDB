package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class GeneMapper extends FeatureMapper {
    
    public static final String SQL = " select * " +
    " from feature f, featureloc fl " +
    " where f.feature_id = fl.feature_id" +
    " and f.type_id in (select cvterm_id from cvterm where name in ('gene', 'pseudogene'))";        
    
    Logger logger = Logger.getLogger(GeneMapper.class);
    
    Map<Integer, OrganismMapper> organisms;
    private SimpleJdbcTemplate template;
    
    
    public GeneMapper(Map<Integer, OrganismMapper> organisms, SimpleJdbcTemplate template){
        this.organisms = organisms;
        this.template = template;
    }
    
    public GeneMapper(OrganismMapper organism, SimpleJdbcTemplate template){
        this.organisms = new HashMap<Integer, OrganismMapper>();
        organisms.put(organism.getOrganismId(), organism);
        this.template = template;
    }
    
    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeatureMapper gene = super.mapRow(rs, rowNum);
        gene.setFmax(rs.getInt("fmax"));
        gene.setFmin(rs.getInt("fmin"));
        gene.setSourceFeatureId(rs.getInt("srcfeature_id"));
        gene.setStrand(rs.getInt("strand"));
        
        OrganismMapper organism = (OrganismMapper)organisms.get(gene.getOrganismId());
        
        
        template.query(TranscriptMapper.SQL, new TranscriptMapper(organism, gene, template), gene.getFeatureId());
        
        return gene;
    }

    
    
}
