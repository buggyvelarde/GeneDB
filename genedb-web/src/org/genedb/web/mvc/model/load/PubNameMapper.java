package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class PubNameMapper implements
ParameterizedRowMapper<String> {

    public static final String SQL = "select uniquename " +
    " from feature_pub fp, pub" +
    " where fp.pub_id = pub.pub_id" +
    " and fp.feature_id = ?" +
    " and pub.uniquename like 'PMID%'";

    @Override
    public String mapRow(ResultSet rs, int rowCount)
    throws SQLException {
        return rs.getString("uniquename");
    }

}