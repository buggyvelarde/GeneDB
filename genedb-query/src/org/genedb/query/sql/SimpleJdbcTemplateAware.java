package org.genedb.query.sql;

//import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Marker interface for classes which want a datasource injected
 * 
 * @author art
 */
public interface SimpleJdbcTemplateAware {

    /**
     * Set up a SQL data source for this class to use
     * 
     * @param ds The datasource
     */
    public void setSimpleJdbcTemplate(SimpleJdbcTemplate sjt);
    
}
