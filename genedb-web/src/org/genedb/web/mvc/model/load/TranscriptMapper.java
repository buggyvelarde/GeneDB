package org.genedb.web.mvc.model.load;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;


public class TranscriptMapper extends FeatureMapper {
    Logger logger = Logger.getLogger(TranscriptMapper.class);

    public static final String SQL_WITH_GENE_ID_PARAM =
    " select f.*, cvt.name as cvtname, cv.name as cvname " +
    " from feature f, feature_relationship fr, cvterm cvt, cv " +
    " where fr.subject_id = f.feature_id " +
    " and f.type_id = cvt.cvterm_id" +
    " and cvt.cv_id = cv.cv_id" +
    " and fr.object_id = ?";



    public static final String SQL_WITH_TRANSCRIPT_ID_PARAMS =
        " select f.*, cvt.name as cvtname, cv.name as cvname " +
        " from feature f,  cvterm cvt, cv " +
        " where feature_id in (:placeholders)" +
        " and f.type_id = cvt.cvterm_id" +
        " and cvt.cv_id = cv.cv_id";

    public static final String SQL_WITH_POLYPEPTIDE_ID_PARAMS =
        " select f.*, cvt.name as cvtname, cv.name as cvname " +
        " from feature f, feature_relationship fr, cvterm cvt, cv " +
        " where fr.object_id = f.feature_id " +
        " and f.type_id = cvt.cvterm_id" +
        " and cvt.cv_id = cv.cv_id" +
        " and fr.subject_id in (:placeholders)";



    @Override
    public FeatureMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        //Get transcript details
        FeatureMapper transcriptMapper = super.mapRow(rs, rowNum);
        transcriptMapper.setCvtName(rs.getString("cvtname"));
        transcriptMapper.setCvName(rs.getString("cvname"));
        return transcriptMapper;
    }
}
