package org.genedb.query.sql;

import javax.sql.DataSource;

/**
 * Marker interface for classes which want a datasource injected
 * 
 * @author art
 */
public interface DataSourceAware {

    /**
     * Set up a SQL data source for this class to use
     * 
     * @param ds The datasource
     */
    public void setDataSource(DataSource ds);
    
}
