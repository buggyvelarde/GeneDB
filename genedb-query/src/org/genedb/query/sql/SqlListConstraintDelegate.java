package org.genedb.query.sql;

import org.genedb.query.params.ListConstraint;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;


/**
 * Class, designed to be used as a delegate by a Param, which maintains
 * a RDBMS generated list of acceptable values for the Param.
 *
 * @author art
 */
public class SqlListConstraintDelegate implements ListConstraint, SimpleJdbcTemplateAware {

    private String validSql;
    private String allSql;
    private String partialSql;
    private SimpleJdbcTemplate sjt;

    public void setSimpleJdbcTemplate(SimpleJdbcTemplate sjt) {
        this.sjt = sjt;
    }

    public void setAllSql(String allSql) {
        this.allSql = allSql;
    }

    public void setPartialSql(String partialSql) {
        this.partialSql = partialSql;
    }

    public void setValidSql(String validSql) {
        this.validSql = validSql;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAcceptableValues(final String partName, final boolean mustBePrefix) {
        JdbcTemplate jt = (JdbcTemplate) sjt.getJdbcOperations();
        return (List<String>) jt.query(partialSql, new Object[]{mustBePrefix}, new ResultSetExtractor() {
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            String test = rs.getString(0);
            int index = test.indexOf(partName);
            if (index == -1) {
            return null;
            }
            if (mustBePrefix && index != 0) {
            return null;
            }
            return test;
        }
        }
        );
    }

    public List<String> getAllAcceptableValues() {
        JdbcTemplate jt = (JdbcTemplate) sjt.getJdbcOperations();
        return (List<String>) jt.queryForList(allSql, new Object[]{}, String.class);
    }

    public boolean isValid(String value) {
        Integer i = sjt.queryForInt(validSql, value);
        if (i.intValue() == 1) {
        return true;
        }
        return false;
    }

}
