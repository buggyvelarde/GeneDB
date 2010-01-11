package org.genedb.web.mvc.model.simple;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleTranscriptMapper extends SimpleFeatureMapper implements RowMapper<SimpleTranscript> {
    Logger logger = Logger.getLogger(SimpleTranscriptMapper.class);

    public static final String SQL_WITH_GENE_ID_PARAM =
            " select f.*, cvt.name as cvtname, cv.name as cvname "
                    + " from feature f, feature_relationship fr, cvterm cvt, cv "
                    + " where fr.subject_id = f.feature_id " + " and f.type_id = cvt.cvterm_id"
                    + " and cvt.cv_id = cv.cv_id" + " and fr.object_id = ?";

    public static final String SQL_WITH_TRANSCRIPT_ID_PARAMS =
            " select f.*, cvt.name as cvtname, cv.name as cvname " + " from feature f,  cvterm cvt, cv "
                    + " where feature_id in (:placeholders)" + " and f.type_id = cvt.cvterm_id"
                    + " and cvt.cv_id = cv.cv_id";

    public static final String SQL_WITH_POLYPEPTIDE_ID_PARAMS =
            " select f.*, cvt.name as cvtname, cv.name as cvname "
                    + " from feature f, feature_relationship fr, cvterm cvt, cv "
                    + " where fr.object_id = f.feature_id " + " and f.type_id = cvt.cvterm_id"
                    + " and cvt.cv_id = cv.cv_id" + " and fr.subject_id in (:placeholders)";

    @Override
    public SimpleTranscript mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Get transcript details
        SimpleTranscript transcript = new SimpleTranscript();
        super.mapRow(transcript, rs);
        transcript.setCvtName(rs.getString("cvtname"));
        return transcript;
    }
}
