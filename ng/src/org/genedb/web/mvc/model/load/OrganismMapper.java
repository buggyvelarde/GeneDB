package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class OrganismMapper  implements ParameterizedRowMapper<OrganismMapper>{    
    Logger logger = Logger.getLogger(OrganismMapper.class);
    
    public static final String GET_ALL_ORGANISMS_SQL =  "select * from organism";
    
    public static final String GET_ALL_ORGANISMS_SQL_WITH_COMMON_NAME_PARAM =   "select * from organism where common_name = ?";
    
    public static final String SQL_WITH_GENE_ID_PARAM = " select o.* " +
        " from feature f, organism o " +
        " where f.organism_id = o.organism_id" +
        " and f.feature_id = ?";
    
    private int organismId;
    private String genus;
    private String species;
    private String commonName;

    @Override
    public OrganismMapper mapRow(ResultSet rs, int rowCount) throws SQLException {
        logger.debug("Enter mapRow");
        OrganismMapper organism = new OrganismMapper();
        organism.setOrganismId(rs.getInt("organism_id"));
        organism.setGenus(rs.getString("genus"));
        organism.setSpecies(rs.getString("species"));
        organism.setCommonName(rs.getString("common_name"));
        logger.debug("Exit mapRow");
        return organism;
    }
    

    
    public int getOrganismId() {
        return organismId;
    }
    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }
    public String getGenus() {
        return genus;
    }
    public void setGenus(String genus) {
        this.genus = genus;
    }
    public String getSpecies() {
        return species;
    }
    public void setSpecies(String species) {
        this.species = species;
    }
    public String getCommonName() {
        return commonName;
    }
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

}
