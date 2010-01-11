package org.genedb.web.mvc.model.load;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.genedb.web.mvc.model.types.DBXRefType;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


public class FeatureRelationshipMapper implements ParameterizedRowMapper<FeatureRelationshipMapper>  {

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static final String TYPE_RESTRICTED_SQL =
        " select fr.*" +
        " from feature_relationship fr, cvterm cvt, cv" +
        " where fr.type_id = cvt.cvterm_id" +
        " and cvt.cv_id = cv.cv_id" +
        " and fr.subject_id = ?" +
        " and cvt.name = ?" +
        " and cv.name = ?";
    Logger logger = Logger.getLogger(FeatureRelationshipMapper.class);


    private SimpleJdbcTemplate template;

    private int subjectId;
    private int objectId;
    private int typeId;

    private String cvtermName;
    private String cvName;

    @Override
    public FeatureRelationshipMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
        //Get frMapper details
        FeatureRelationshipMapper frMapper = new FeatureRelationshipMapper();
        frMapper.setSubjectId(rs.getInt("subject_id"));
        frMapper.setObjectId(rs.getInt("object_id"));
        frMapper.setCvtermName(rs.getString("cvtname"));
        frMapper.setCvName(rs.getString("cvname"));

        return frMapper;
    }

    public SimpleJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SimpleJdbcTemplate template) {
        this.template = template;
    }


    public String getCvtermName() {
        return cvtermName;
    }

    public void setCvtermName(String cvtermName) {
        this.cvtermName = cvtermName;
    }

    public String getCvName() {
        return cvName;
    }

    public void setCvName(String cvName) {
        this.cvName = cvName;
    }
}
