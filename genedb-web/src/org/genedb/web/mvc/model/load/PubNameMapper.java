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
    
    public static final String FEATURE_CVTERM_SQL = "select uniquename " +
    	" from feature_cvterm fcvt, feature_cvterm_pub fcvtpub, pub" +
    	" where fcvt.feature_cvterm_id = ?" +
    	" fcvt.feature_cvterm_id = fcvtpub.feature_cvterm_id" +
    	" and fcvtpub.pub_id = pub.pub_id";

    @Override
    public String mapRow(ResultSet rs, int rowCount)
    throws SQLException {
        return rs.getString("uniquename");
    }

}