package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ClusterIdAndOrthologueNamesMapper implements ParameterizedRowMapper<ClusterIdAndOrthologueNamesMapper> {
    
    public static final String SQL = 
            " select f.uniquename, cvt.name " +
    		" from feature f, cvterm cvt " +
    		" where f.type_id = cvt.cvterm_id" +
    		" and cvt.name in ('protein_match', 'polypeptide')" +
    		" and f.feature_id in (" +
    		"     select fr.object_id" +
    		"     from feature_relationship fr, cvterm cvt, cv" +
    		"     where fr.type_id = cvt.cvterm_id" +
    		"     and cvt.cv_id = cv.cv_id" +
    		"     and cvt.name = 'orthologous_to'" +
    		"     and cv.name = 'sequence'" +
    		"     and fr.subject_id = ?)";
    
    private String uniqueName;
    private String cvtName;
    
    @Override
    public ClusterIdAndOrthologueNamesMapper mapRow(ResultSet rs, int rowCount)
            throws SQLException {
        ClusterIdAndOrthologueNamesMapper mapper = new ClusterIdAndOrthologueNamesMapper();
        mapper.setCvtName(rs.getString("name"));
        mapper.setUniqueName(rs.getString("uniquename"));
        return mapper;
    }
    
    public String getUniqueName() {
        return uniqueName;
    }
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    public String getCvtName() {
        return cvtName;
    }
    public void setCvtName(String cvtName) {
        this.cvtName = cvtName;
    }
    
}
