package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.SynonymType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * 
 * @author lo2@sangerinstitute
 *
 */
public class SynonymTypeMapper  implements ParameterizedRowMapper<SynonymType>{
    public static final String SQL = "select fs.is_current, s.name as sname, cvt.name as cvtname " +
    		" from feature_synonym fs, synonym s, cvterm cvt" +
    		" where fs.feature_id = ?" +
    		" and fs.synonym_id = s.synonym_id" +
    		" and s.type_id = cvt.cvterm_id"; 
    
    
    @Override
    public SynonymType mapRow(ResultSet rs, int rowCount)
            throws SQLException {
        SynonymType synonymType = new SynonymType();
        synonymType.setCurrent(rs.getBoolean("is_current"));
        synonymType.setSynonymName(rs.getString("sname").replace(",", ""));
        synonymType.setCvtName(rs.getString("cvtname"));
        return synonymType;
    }
}
