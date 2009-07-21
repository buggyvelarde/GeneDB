package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.DBXRefType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class DbxRefMapper implements ParameterizedRowMapper<DBXRefType> {

    
    public static final String SQL ="select accession, db.name, db.urlprefix " +
    		" from feature_dbxref fdbx, dbxref, db" +
    		" where fdbx.feature_id = ?" +
    		" and fdbx.dbxref_id = dbxref.dbxref_id" +
    		" and dbxref.db_id = db.db_id";
    
    @Override
    public DBXRefType mapRow(ResultSet rs, int rowCount) throws SQLException {
        return new DBXRefType(
                rs.getString("accession"),
                rs.getString("name"),
                rs.getString("urlprefix"));
    }

}
