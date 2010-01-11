package org.genedb.web.mvc.model.simple;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimplePolypeptideMapper extends SimpleFeatureMapper implements RowMapper<SimplePolypeptide> {

    public static final String SQL =
            "select f.* " + " from feature f, feature_relationship fr, cvterm cvt, cv" + " where fr.object_id = ?"
                    + " and fr.subject_id = f.feature_id" + " and fr.type_id = cvt.cvterm_id"
                    + " and cvt.name = 'derives_from'" + " and cv.name = 'sequence'";

    @Override
    public SimplePolypeptide mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimplePolypeptide ret = new SimplePolypeptide();
        super.mapRow(ret, rs);
        return ret;
    }
}
