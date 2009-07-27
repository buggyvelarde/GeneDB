package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.TranscriptRegionType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class TranscriptRegionMapper implements
        ParameterizedRowMapper<TranscriptRegionType> {
    
    public static final String SQL = 
        " select f.feature_id, f.uniquename, fl.fmin, fl.fmax, cvt.name" +
        " from feature_relationship fr, feature f, featureLoc fl, cvterm cvt" +
        " where fr.object_id = ?" +
        " and fr.subject_id = f.feature_id" +
        " and f.feature_id = fl.feature_id" +
        " and f.type_id = cvt.cvterm_id" +
        " and cvt.name in ('exon', 'pseudogenic_exon', 'five_prime_UTR', 'three_prime_UTR')";

    @Override
    public TranscriptRegionType mapRow(ResultSet rs, int rowCount)
            throws SQLException {
       TranscriptRegionType transcriptRegionType = new TranscriptRegionType();
       transcriptRegionType.setFeatureId(rs.getInt("feature_id"));
       transcriptRegionType.setUniquename(rs.getString("uniquename"));
       transcriptRegionType.setFmin(rs.getInt("fmin"));
       transcriptRegionType.setFmax(rs.getInt("fmax"));
       transcriptRegionType.setCvtermName(rs.getString("name"));
        return transcriptRegionType;
    }

}
